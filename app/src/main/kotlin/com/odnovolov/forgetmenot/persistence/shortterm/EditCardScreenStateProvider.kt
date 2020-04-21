package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.shortterm.EditCardScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class EditCardScreenStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = EditCardScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<EditCardScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val cardId: Long,
        val isExerciseOpened: Boolean,
        val questionInput: String,
        val answerInput: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: EditCardScreenState) = SerializableState(
        state.card.id,
        state.isExerciseOpened,
        state.questionInput,
        state.answerInput
    )

    override fun toOriginal(serializableState: SerializableState): EditCardScreenState {
        val card: Card = globalState.decks.asSequence()
            .flatMap { deck: Deck -> deck.cards.asSequence() }
            .first { card: Card -> card.id == serializableState.cardId }
        return EditCardScreenState(
            card,
            serializableState.isExerciseOpened,
            serializableState.questionInput,
            serializableState.answerInput
        )
    }
}