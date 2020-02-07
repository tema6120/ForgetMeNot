package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.adddeck.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.adddeck.Stage
import kotlinx.serialization.Serializable

object AddDeckStateProvider {
    fun load(): AddDeckInteractor.State {
        return loadSerializable(SerializableAddDeckState.serializer())
            ?.toOriginal()
            ?: AddDeckInteractor.State()
    }

    fun save(state: AddDeckInteractor.State) {
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

    private fun AddDeckInteractor.State.toSerializable() = SerializableAddDeckState(
        stage,
        cardPrototypes
    )

    private fun SerializableAddDeckState.toOriginal() = AddDeckInteractor.State().apply {
        stage = this@toOriginal.stage
        cardPrototypes = this@toOriginal.cardPrototypes
    }
}