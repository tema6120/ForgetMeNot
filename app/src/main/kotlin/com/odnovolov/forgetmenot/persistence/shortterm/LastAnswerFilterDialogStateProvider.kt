package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.persistence.shortterm.LastAnswerFilterDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDialogState
import kotlinx.serialization.Serializable

class LastAnswerFilterDialogStateProvider
    : BaseSerializableStateProvider<LastAnswerFilterDialogState, SerializableState>() {
    @Serializable
    data class SerializableState(
        val isFromDialog: Boolean,
        var isZeroTimeSelected: Boolean,
        val intervalInputValue: Int?,
        val intervalUnit: IntervalUnit
    )

    override val serializer = SerializableState.serializer()
    override val serializableId: String = SerializableState::class.simpleName!!

    override fun toSerializable(state: LastAnswerFilterDialogState) = SerializableState(
        state.isFromDialog,
        state.isZeroTimeSelected,
        state.timeAgo.value,
        state.timeAgo.intervalUnit
    )

    override fun toOriginal(serializableState: SerializableState): LastAnswerFilterDialogState {
        val timeAgo = DisplayedInterval(
            serializableState.intervalInputValue,
            serializableState.intervalUnit
        )
        return LastAnswerFilterDialogState(
            serializableState.isFromDialog,
            serializableState.isZeroTimeSelected,
            timeAgo
        )
    }
}