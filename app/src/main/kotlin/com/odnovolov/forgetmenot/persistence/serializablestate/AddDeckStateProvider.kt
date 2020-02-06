package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck
import com.odnovolov.forgetmenot.domain.interactor.adddeck.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.adddeck.Stage
import kotlinx.serialization.Serializable

object AddDeckStateProvider {
    fun load(): AddDeck.State {
        return loadSerializable(SerializableAddDeckState.serializer())
            ?.toOriginal()
            ?: AddDeck.State()
    }

    fun save(state: AddDeck.State) {
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

    private fun AddDeck.State.toSerializable() = SerializableAddDeckState(
        stage,
        cardPrototypes
    )

    private fun SerializableAddDeckState.toOriginal() = AddDeck.State().apply {
        stage = this@toOriginal.stage
        cardPrototypes = this@toOriginal.cardPrototypes
    }
}