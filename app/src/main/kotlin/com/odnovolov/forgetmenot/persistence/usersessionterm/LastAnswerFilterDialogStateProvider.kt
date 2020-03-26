package com.odnovolov.forgetmenot.persistence.usersessionterm

import com.odnovolov.forgetmenot.persistence.usersessionterm.LastAnswerFilterDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswerfiltereditor.LastAnswerFilterDialogState
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
    override val serializableClassName: String = SerializableState::class.java.name

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