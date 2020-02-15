package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import kotlinx.serialization.Serializable

object AddDeckScreenStateProvider {
    fun load(): AddDeckScreenState {
        return loadSerializable(SerializableAddDeckScreenState.serializer())
            ?.toOriginal()
            ?: AddDeckScreenState()
    }

    fun save(addDeckScreenState: AddDeckScreenState) {
        val serializable = addDeckScreenState.toSerializable()
        saveSerializable(serializable, SerializableAddDeckScreenState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableAddDeckScreenState::class)
    }

    @Serializable
    private data class SerializableAddDeckScreenState(
        val typedText: String
    )

    private fun AddDeckScreenState.toSerializable() = SerializableAddDeckScreenState(
        typedText
    )

    private fun SerializableAddDeckScreenState.toOriginal() = AddDeckScreenState().apply {
        typedText = this@toOriginal.typedText
    }
}