package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.ModifyIntervalDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import kotlinx.serialization.Serializable

class ModifyIntervalDialogStateProvider(
    override val key: String = ModifyIntervalDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<ModifyIntervalDialogState, SerializableState>() {
    @Serializable
    data class SerializableState(
        val targetLevelOfKnowledge: Int,
        val intervalInputValue: Int?,
        val intervalUnit: IntervalUnit
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: ModifyIntervalDialogState) =
        SerializableState(
            targetLevelOfKnowledge = state.targetLevelOfKnowledge,
            intervalInputValue = state.displayedInterval.value,
            intervalUnit = state.displayedInterval.intervalUnit
        )

    override fun toOriginal(
        serializableState: SerializableState
    ): ModifyIntervalDialogState {
        val intervalInputData =
            DisplayedInterval(
                serializableState.intervalInputValue,
                serializableState.intervalUnit
            )
        return ModifyIntervalDialogState(
            serializableState.targetLevelOfKnowledge,
            intervalInputData
        )
    }
}