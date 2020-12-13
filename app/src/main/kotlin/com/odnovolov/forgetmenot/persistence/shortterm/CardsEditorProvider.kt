package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardsEditorProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = CardsEditor::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsEditor, SerializableState>(
    json,
    database
) {
    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardsEditor): SerializableState {
        val cardsEditor = state
        val serializableEditableCards: List<SerializableEditableCard> =
            cardsEditor.state.editableCards
                .map { editableCard: EditableCard -> editableCard.toSerializable() }
        return when (cardsEditor) {
            is CardsEditorForDeckCreation -> {
                SerializableStateForDeckCreation(
                    cardsEditor.deckName,
                    serializableEditableCards,
                    cardsEditor.state.currentPosition
                )
            }
            is CardsEditorForEditingExistingDeck -> {
                SerializableStateForEditingExistingDeck(
                    cardsEditor.deck.id,
                    serializableEditableCards,
                    cardsEditor.state.currentPosition
                )
            }
            is CardsEditorForEditingSpecificCards -> {
                val removedSerializableEditableCards =
                    cardsEditor.removedCards.map { editableCard: EditableCard ->
                        editableCard.toSerializable()
                    }
                SerializableStateForEditingSpecificCards(
                    removedSerializableEditableCards,
                    serializableEditableCards,
                    cardsEditor.state.currentPosition
                )
            }
            else -> {
                error("Type is not supported")
            }
        }
    }

    private fun EditableCard.toSerializable() = SerializableEditableCard(
        card.id,
        deck?.id,
        question,
        answer,
        isLearned,
        grade
    )

    override fun toOriginal(serializableState: SerializableState): CardsEditor {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val editableCards: List<EditableCard> = serializableState.serializableEditableCards
            .map { serializableEditableCard: SerializableEditableCard ->
                val card: Card = cardIdCardMap[serializableEditableCard.cardId]
                    ?: Card(serializableEditableCard.cardId, "", "")
                val deck: Deck? =
                    serializableEditableCard.deckId?.let { deckIdDeckMap.getValue(it) }
                EditableCard(
                    card,
                    deck,
                    serializableEditableCard.question,
                    serializableEditableCard.answer,
                    serializableEditableCard.isLearned,
                    serializableEditableCard.grade
                )
            }
        val cardsEditorState = CardsEditor.State(
            editableCards,
            serializableState.currentPosition
        )
        return when (serializableState) {
            is SerializableStateForDeckCreation -> {
                CardsEditorForDeckCreation(
                    serializableState.deckName,
                    globalState,
                    cardsEditorState
                )
            }
            is SerializableStateForEditingExistingDeck -> {
                val deck: Deck = deckIdDeckMap.getValue(serializableState.deckId)
                CardsEditorForEditingExistingDeck(
                    deck,
                    cardsEditorState
                )
            }
            is SerializableStateForEditingSpecificCards -> {
                val removedEditableCards: MutableList<EditableCard> =
                    serializableState.removedSerializableEditableCards
                        .map { serializableEditableCard: SerializableEditableCard ->
                            val card: Card = cardIdCardMap.getValue(serializableEditableCard.cardId)
                            val deck: Deck? =
                                serializableEditableCard.deckId?.let { deckIdDeckMap.getValue(it) }
                            EditableCard(
                                card,
                                deck,
                                serializableEditableCard.question,
                                serializableEditableCard.answer,
                                serializableEditableCard.isLearned,
                                serializableEditableCard.grade
                            )
                        }
                        .toMutableList()
                when {
                    ExerciseDiScope.isOpen() -> {
                        CardsEditorForExercise(
                            ExerciseDiScope.get()!!.exercise,
                            removedEditableCards,
                            cardsEditorState
                        )
                    }
                    RepetitionDiScope.isOpen() -> {
                        CardsEditorForRepetition(
                            RepetitionDiScope.get()!!.repetition,
                            removedEditableCards,
                            cardsEditorState
                        )
                    }
                    else -> {
                        CardsEditorForEditingSpecificCards(
                            removedEditableCards,
                            cardsEditorState
                        )
                    }
                }
            }
        }
    }
}

@Serializable
sealed class SerializableState {
    abstract val serializableEditableCards: List<SerializableEditableCard>
    abstract val currentPosition: Int
}

@Serializable
class SerializableStateForDeckCreation(
    val deckName: String,
    override val serializableEditableCards: List<SerializableEditableCard>,
    override val currentPosition: Int
) : SerializableState()

@Serializable
class SerializableStateForEditingExistingDeck(
    val deckId: Long,
    override val serializableEditableCards: List<SerializableEditableCard>,
    override val currentPosition: Int
) : SerializableState()

@Serializable
class SerializableStateForEditingSpecificCards(
    val removedSerializableEditableCards: List<SerializableEditableCard>,
    override val serializableEditableCards: List<SerializableEditableCard>,
    override val currentPosition: Int
) : SerializableState()

@Serializable
data class SerializableEditableCard(
    val cardId: Long,
    val deckId: Long?,
    val question: String,
    val answer: String,
    val isLearned: Boolean,
    val grade: Int
)