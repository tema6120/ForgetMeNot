package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.OngoingCardEditor
import com.odnovolov.forgetmenot.persistence.shortterm.OngoingCardEditorStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OngoingCardEditorStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = OngoingCardEditor::class.qualifiedName!!
) : BaseSerializableStateProvider<EditableCard, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val cardId: Long,
        val questionInput: String,
        val answerInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: EditableCard) = SerializableState(
        state.card!!.id,
        state.question,
        state.answer
    )

    override fun toOriginal(serializableState: SerializableState): EditableCard {
        val card: Card = globalState.decks.asSequence()
            .flatMap { deck: Deck -> deck.cards.asSequence() }
            .first { card: Card -> card.id == serializableState.cardId }
        return EditableCard(card).apply {
            question = serializableState.questionInput
            answer = serializableState.answerInput
        }
    }
}