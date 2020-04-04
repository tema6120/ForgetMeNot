package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.domain.interactor.deckadder.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.Stage
import com.odnovolov.forgetmenot.persistence.shortterm.AddDeckStateProvider.SerializableState
import kotlinx.serialization.Serializable

class AddDeckStateProvider(
    override val key: String = DeckAdder.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckAdder.State, SerializableState>() {
    @Serializable
    data class SerializableState(
        val stage: Stage,
        val cardPrototypes: List<CardPrototype>?
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckAdder.State) = SerializableState(
        state.stage,
        state.cardPrototypes
    )

    override fun toOriginal(serializableState: SerializableState) = DeckAdder.State().apply {
        stage = serializableState.stage
        cardPrototypes = serializableState.cardPrototypes
    }
}