package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionCreatorStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RepetitionCreatorStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = RepetitionStateCreator.State::class.qualifiedName!!
) : BaseSerializableStateProvider<RepetitionStateCreator.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val deckIds: List<Long>
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: RepetitionStateCreator.State) = SerializableState(
        deckIds = state.decks.map { it.id }
    )

    override fun toOriginal(serializableState: SerializableState) = RepetitionStateCreator.State(
        globalState.decks.filter { it.id in serializableState.deckIds }
    )
}