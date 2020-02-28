package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsScreenState
import kotlinx.serialization.Serializable

object IntervalsScreenStateProvider {
    fun load(): IntervalsScreenState {
        return loadSerializable(SerializableIntervalsScreenState.serializer())
            ?.toOriginal()
            ?: throw  IllegalStateException("No IntervalsScreenState in the Store")
    }

    fun save(state: IntervalsScreenState) {
        val serializable: SerializableIntervalsScreenState = state.toSerializable()
        saveSerializable(serializable, SerializableIntervalsScreenState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableIntervalsScreenState::class)
    }

    @Serializable
    private data class SerializableIntervalsScreenState(
        val namePresetDialogStatus: NamePresetDialogStatus,
        val typedPresetName: String,
        val renamePresetId: Long?
    )

    private fun IntervalsScreenState.toSerializable() = SerializableIntervalsScreenState(
        namePresetDialogStatus,
        typedPresetName,
        renamePresetId
    )

    private fun SerializableIntervalsScreenState.toOriginal() = IntervalsScreenState().also {
        it.namePresetDialogStatus = this.namePresetDialogStatus
        it.typedPresetName = this.typedPresetName
        it.renamePresetId = this.renamePresetId
    }
}