package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.*
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardsEditorProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = CardsEditor::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsEditor, SerializableCardsEditorState>(
    json,
    database
) {
    override val serializer = SerializableCardsEditorState.serializer()

    override fun toSerializable(state: CardsEditor): SerializableCardsEditorState {
        val cardsEditor = state
        val serializableEditableCards: List<SerializableEditableCard> =
            cardsEditor.state.editableCards
                .map { editableCard: EditableCard -> editableCard.toSerializable() }
        val serializableRemovals: List<SerializableCardRemoving> =
            cardsEditor.state.removals
                .map { cardRemoving: CardRemoving -> cardRemoving.toSerializable() }
        val serializableMovements: List<SerializableCardMoving> =
            cardsEditor.state.movements
                .map { cardMoving: CardMoving -> cardMoving.toSerializable() }
        val serializableCopyOperations: List<SerializableCardCopying> =
            cardsEditor.state.copyOperations
                .map { cardCopying: CardCopying -> cardCopying.toSerializable() }
        val createdDeckIds: List<Long> =
            cardsEditor.state.createdDecks
                .map { deck: Deck -> deck.id }
        return when (cardsEditor) {
            is CardsEditorForEditingDeck -> {
                SerializableCardsEditorStateForEditingDeck(
                    cardsEditor.deck.id,
                    cardsEditor.isNewDeck,
                    serializableEditableCards,
                    cardsEditor.state.currentPosition,
                    serializableRemovals,
                    serializableMovements,
                    serializableCopyOperations,
                    createdDeckIds
                )
            }
            is CardsEditorForEditingSpecificCards -> {
                val screen: EditingSpecificCardsScreen = when {
                    cardsEditor.exercise != null -> EditingSpecificCardsScreen.Exercise
                    cardsEditor.player != null -> EditingSpecificCardsScreen.Player
                    else -> EditingSpecificCardsScreen.Other
                }
                SerializableStateForEditingSpecificCards(
                    serializableEditableCards,
                    cardsEditor.state.currentPosition,
                    serializableRemovals,
                    serializableMovements,
                    serializableCopyOperations,
                    createdDeckIds,
                    screen
                )
            }
            else -> {
                error("Type is not supported")
            }
        }
    }

    private fun EditableCard.toSerializable() = SerializableEditableCard(
        card.id,
        deck.id,
        question,
        answer,
        isLearned,
        grade
    )

    private fun CardRemoving.toSerializable() = SerializableCardRemoving(
        editableCard.toSerializable(),
        positionInSource
    )

    private fun CardMoving.toSerializable() = SerializableCardMoving(
        editableCard.toSerializable(),
        positionInSource,
        targetDeck.id
    )

    private fun CardCopying.toSerializable() = SerializableCardCopying(
        copyOfQuestion,
        copyOfAnswer,
        targetDeck.id
    )

    override fun toOriginal(serializableState: SerializableCardsEditorState): CardsEditor {
        val deckIdDeckMap: Map<Long, Deck> = globalState.decks.associateBy { deck -> deck.id }
        val cardIdCardMap: Map<Long, Card> = globalState.decks
            .flatMap { deck -> deck.cards }
            .associateBy { card -> card.id }
        val editableCards: List<EditableCard> =
            serializableState.serializableEditableCards
                .map { serializableEditableCard: SerializableEditableCard ->
                    serializableEditableCard.toOriginal(deckIdDeckMap, cardIdCardMap)
                }
        val removals: MutableList<CardRemoving> =
            serializableState.serializableRemovals
                .map { serializableCardRemoving: SerializableCardRemoving ->
                    serializableCardRemoving.toOriginal(deckIdDeckMap, cardIdCardMap)
                }
                .toMutableList()
        val movements: MutableList<CardMoving> =
            serializableState.serializableMovements
                .map { serializableCardMoving: SerializableCardMoving ->
                    serializableCardMoving.toOriginal(deckIdDeckMap, cardIdCardMap)
                }
                .toMutableList()
        val copyOperations: MutableList<CardCopying> =
            serializableState.serializableCopyOperations
                .map { serializableCardCopying: SerializableCardCopying ->
                    serializableCardCopying.toOriginal(deckIdDeckMap)
                }
                .toMutableList()
        val createdDecks: MutableList<Deck> =
            serializableState.createdDeckIds
                .map { createdDeckId: Long -> deckIdDeckMap.getValue(createdDeckId) }
                .toMutableList()
        val cardsEditorState = CardsEditor.State(
            editableCards,
            serializableState.currentPosition,
            removals,
            movements,
            copyOperations,
            createdDecks
        )
        return when (serializableState) {
            is SerializableCardsEditorStateForEditingDeck -> {
                val deck = deckIdDeckMap.getValue(serializableState.deckId)
                CardsEditorForEditingDeck(
                    deck,
                    serializableState.isNewDeck,
                    cardsEditorState,
                    globalState
                )
            }
            is SerializableStateForEditingSpecificCards -> {
                val exercise: Exercise? =
                    if (serializableState.screen == EditingSpecificCardsScreen.Exercise) {
                        ExerciseDiScope.getOrRecreate().exercise
                    } else {
                        null
                    }
                val player: Player? =
                    if (serializableState.screen == EditingSpecificCardsScreen.Player) {
                        PlayerDiScope.getOrRecreate().player
                    } else {
                        null
                    }
                CardsEditorForEditingSpecificCards(
                    cardsEditorState,
                    globalState,
                    exercise,
                    player
                )
            }
        }
    }

    private fun SerializableEditableCard.toOriginal(
        deckIdDeckMap: Map<Long, Deck>,
        cardIdCardMap: Map<Long, Card>
    ): EditableCard {
        val card: Card = cardIdCardMap[cardId] ?: Card(cardId, question = "", answer = "")
        val deck: Deck = deckIdDeckMap.getValue(deckId)
        return EditableCard(card, deck, question, answer, isLearned, grade)
    }

    private fun SerializableCardRemoving.toOriginal(
        deckIdDeckMap: Map<Long, Deck>,
        cardIdCardMap: Map<Long, Card>
    ): CardRemoving {
        val editableCard: EditableCard = serializableEditableCard.toOriginal(
            deckIdDeckMap,
            cardIdCardMap
        )
        return CardRemoving(editableCard, positionInSource)
    }

    private fun SerializableCardMoving.toOriginal(
        deckIdDeckMap: Map<Long, Deck>,
        cardIdCardMap: Map<Long, Card>
    ): CardMoving {
        val editableCard: EditableCard = serializableEditableCard.toOriginal(
            deckIdDeckMap,
            cardIdCardMap
        )
        val targetDeck: Deck = deckIdDeckMap.getValue(targetDeckId)
        return CardMoving(editableCard, positionInSource, targetDeck)
    }

    private fun SerializableCardCopying.toOriginal(
        deckIdDeckMap: Map<Long, Deck>
    ): CardCopying {
        val targetDeck: Deck = deckIdDeckMap.getValue(targetDeckId)
        return CardCopying(copyOfQuestion, copyOfAnswer, targetDeck)
    }
}

@Serializable
sealed class SerializableCardsEditorState {
    abstract val serializableEditableCards: List<SerializableEditableCard>
    abstract val currentPosition: Int
    abstract val serializableRemovals: List<SerializableCardRemoving>
    abstract val serializableMovements: List<SerializableCardMoving>
    abstract val serializableCopyOperations: List<SerializableCardCopying>
    abstract val createdDeckIds: List<Long>
}

@Serializable
class SerializableCardsEditorStateForEditingDeck(
    val deckId: Long,
    val isNewDeck: Boolean,
    override val serializableEditableCards: List<SerializableEditableCard>,
    override val currentPosition: Int,
    override val serializableRemovals: List<SerializableCardRemoving>,
    override val serializableMovements: List<SerializableCardMoving>,
    override val serializableCopyOperations: List<SerializableCardCopying>,
    override val createdDeckIds: List<Long>
) : SerializableCardsEditorState()

@Serializable
class SerializableStateForEditingSpecificCards(
    override val serializableEditableCards: List<SerializableEditableCard>,
    override val currentPosition: Int,
    override val serializableRemovals: List<SerializableCardRemoving>,
    override val serializableMovements: List<SerializableCardMoving>,
    override val serializableCopyOperations: List<SerializableCardCopying>,
    override val createdDeckIds: List<Long>,
    val screen: EditingSpecificCardsScreen
) : SerializableCardsEditorState()

enum class EditingSpecificCardsScreen {
    Exercise,
    Player,
    Other
}

@Serializable
data class SerializableEditableCard(
    val cardId: Long,
    val deckId: Long,
    val question: String,
    val answer: String,
    val isLearned: Boolean,
    val grade: Int
)

@Serializable
data class SerializableCardRemoving(
    val serializableEditableCard: SerializableEditableCard,
    val positionInSource: Int
)

@Serializable
data class SerializableCardMoving(
    val serializableEditableCard: SerializableEditableCard,
    val positionInSource: Int,
    val targetDeckId: Long
)

@Serializable
data class SerializableCardCopying(
    val copyOfQuestion: String,
    val copyOfAnswer: String,
    val targetDeckId: Long
)