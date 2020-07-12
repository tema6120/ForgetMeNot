package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.State.Stage
import com.odnovolov.forgetmenot.persistence.shortterm.DeckFromFileCreatorStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckFromFileCreatorStateProvider(
    json: Json,
    database: Database,
    override val key: String = DeckFromFileCreator.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckFromFileCreator.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val stage: Stage,
        val cardPrototypes: List<CardPrototype>?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckFromFileCreator.State) = SerializableState(
        state.stage,
        state.cardPrototypes
    )

    override fun toOriginal(serializableState: SerializableState) = DeckFromFileCreator.State().apply {
        stage = serializableState.stage
        cardPrototypes = serializableState.cardPrototypes
    }
}