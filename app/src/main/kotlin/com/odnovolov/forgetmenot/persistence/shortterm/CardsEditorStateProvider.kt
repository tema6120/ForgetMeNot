package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.Creation
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.EditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.persistence.shortterm.CardsEditorStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CardsEditorStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = CardsEditor.State::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsEditor.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckId: Long?,
        val deckName: String?,
        val serializableEditableCards: List<SerializableEditableCard>,
        val currentPosition: Int
    )

    @Serializable
    data class SerializableEditableCard(
        val cardId: Long,
        val question: String,
        val answer: String,
        val isLearned: Boolean,
        val levelOfKnowledge: Int
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardsEditor.State): SerializableState {
        val serializableEditableCards: List<SerializableEditableCard> = state.editableCards
            .map { editableCard: EditableCard ->
                SerializableEditableCard(
                    editableCard.card.id,
                    editableCard.question,
                    editableCard.answer,
                    editableCard.isLearned,
                    editableCard.levelOfKnowledge
                )
            }
        val mode: CardsEditor.State.Mode = state.mode
        return SerializableState(
            if (mode is EditingExistingDeck) mode.deck.id else null,
            if (mode is Creation) mode.deckName else null,
            serializableEditableCards,
            state.currentPosition
        )
    }

    override fun toOriginal(serializableState: SerializableState): CardsEditor.State {
        var deck: Deck? = null
        val mode: CardsEditor.State.Mode =
            if (serializableState.deckId != null) {
                deck = globalState.decks.first { it.id == serializableState.deckId }
                EditingExistingDeck(deck)
            } else {
                Creation(serializableState.deckName!!)
            }
        val editableCards: List<EditableCard> = serializableState.serializableEditableCards
            .map { serializableEditableCard: SerializableEditableCard ->
                val card: Card = deck?.cards?.find { it.id == serializableEditableCard.cardId }
                    ?: Card(id = serializableEditableCard.cardId, question = "", answer = "")
                EditableCard(card).apply {
                    question = serializableEditableCard.question
                    answer = serializableEditableCard.answer
                    isLearned = serializableEditableCard.isLearned
                    levelOfKnowledge = serializableEditableCard.levelOfKnowledge
                }
            }
        return CardsEditor.State(
            mode,
            editableCards,
            serializableState.currentPosition
        )
    }
}