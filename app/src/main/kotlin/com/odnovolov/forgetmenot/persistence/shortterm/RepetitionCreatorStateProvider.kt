package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionCreatorStateProvider.SerializableState
import kotlinx.serialization.Serializable

class RepetitionCreatorStateProvider(
    val globalState: GlobalState
) : BaseSerializableStateProvider<RepetitionStateCreator.State, SerializableState>() {
    @Serializable
    data class SerializableState(
        val deckIds: List<Long>
    )

    override val serializer = SerializableState.serializer()
    override val serializableClassName: String = SerializableState::class.java.name

    override fun toSerializable(state: RepetitionStateCreator.State) = SerializableState(
        deckIds = state.decks.map { it.id }
    )

    override fun toOriginal(serializableState: SerializableState) = RepetitionStateCreator.State(
        globalState.decks.filter { it.id in serializableState.deckIds }
    )
}