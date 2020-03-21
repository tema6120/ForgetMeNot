package com.odnovolov.forgetmenot.persistence.usersessionterm

import com.odnovolov.forgetmenot.domain.interactor.deckadder.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.Stage
import com.odnovolov.forgetmenot.persistence.usersessionterm.AddDeckStateProvider.SerializableAddDeckState
import kotlinx.serialization.Serializable

class AddDeckStateProvider
    : BaseSerializableStateProvider<DeckAdder.State, SerializableAddDeckState>() {
    @Serializable
    data class SerializableAddDeckState(
        val stage: Stage,
        val cardPrototypes: List<CardPrototype>?
    )

    override val serializer = SerializableAddDeckState.serializer()
    override val serializableClassName: String = SerializableAddDeckState::class.java.name
    override val defaultState = DeckAdder.State()

    override fun toSerializable(state: DeckAdder.State) = SerializableAddDeckState(
        state.stage,
        state.cardPrototypes
    )

    override fun toOriginal(serializableState: SerializableAddDeckState) = DeckAdder.State().apply {
        stage = serializableState.stage
        cardPrototypes = serializableState.cardPrototypes
    }
}