package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import kotlinx.serialization.Serializable

object ModifyIntervalDialogStateProvider {
    fun load(): ModifyIntervalDialogState {
        return loadSerializable(SerializableModifyIntervalDialogState.serializer())
            ?.toOriginal()
            ?: throw  IllegalStateException("No ModifyIntervalDialogState in the Store")
    }

    fun save(state: ModifyIntervalDialogState) {
        val serializable: SerializableModifyIntervalDialogState = state.toSerializable()
        saveSerializable(serializable, SerializableModifyIntervalDialogState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableModifyIntervalDialogState::class)
    }

    @Serializable
    private data class SerializableModifyIntervalDialogState(
        val targetLevelOfKnowledge: Int,
        val intervalInputValue: Int?,
        val intervalUnit: IntervalUnit
    )

    private fun ModifyIntervalDialogState.toSerializable() = SerializableModifyIntervalDialogState(
        targetLevelOfKnowledge = targetLevelOfKnowledge,
        intervalInputValue = displayedInterval.value,
        intervalUnit = displayedInterval.intervalUnit
    )

    private fun SerializableModifyIntervalDialogState.toOriginal(): ModifyIntervalDialogState {
        val intervalInputData = DisplayedInterval(
            intervalInputValue,
            intervalUnit
        )
        return ModifyIntervalDialogState(targetLevelOfKnowledge, intervalInputData)
    }
}