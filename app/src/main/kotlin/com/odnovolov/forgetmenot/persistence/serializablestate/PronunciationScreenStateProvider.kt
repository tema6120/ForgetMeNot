package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import kotlinx.serialization.Serializable

object PronunciationScreenStateProvider {
    fun load(): PronunciationScreenState {
        return loadSerializable(SerializablePronunciationScreenState.serializer())
            ?.toOriginal()
            ?: throw  IllegalStateException("No PronunciationScreenState in the Store")
    }

    fun save(state: PronunciationScreenState) {
        val serializable: SerializablePronunciationScreenState = state.toSerializable()
        saveSerializable(serializable, SerializablePronunciationScreenState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializablePronunciationScreenState::class)
    }

    @Serializable
    private data class SerializablePronunciationScreenState(
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    private fun PronunciationScreenState.toSerializable() = SerializablePronunciationScreenState(
        namePresetDialogStatus,
        typedPresetName,
        renamePresetId
    )

    private fun SerializablePronunciationScreenState.toOriginal() =
        PronunciationScreenState().also {
            it.namePresetDialogStatus = this.namePresetDialogStatus
            it.typedPresetName = this.typedPresetName
            it.renamePresetId = this.renamePresetId
        }
}