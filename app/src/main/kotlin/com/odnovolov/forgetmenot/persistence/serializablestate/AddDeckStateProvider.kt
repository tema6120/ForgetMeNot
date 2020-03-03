package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.deckadder.Stage
import kotlinx.serialization.Serializable

object AddDeckStateProvider {
    fun load(): DeckAdder.State {
        return loadSerializable(SerializableAddDeckState.serializer())
            ?.toOriginal()
            ?: DeckAdder.State()
    }

    fun save(state: DeckAdder.State) {
        val serializable = state.toSerializable()
        saveSerializable(serializable, SerializableAddDeckState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableAddDeckState::class)
    }

    @Serializable
    private data class SerializableAddDeckState(
        val stage: Stage,
        val cardPrototypes: List<CardPrototype>?
    )

    private fun DeckAdder.State.toSerializable() = SerializableAddDeckState(
        stage,
        cardPrototypes
    )

    private fun SerializableAddDeckState.toOriginal() = DeckAdder.State().apply {
        stage = this@toOriginal.stage
        cardPrototypes = this@toOriginal.cardPrototypes
    }
}