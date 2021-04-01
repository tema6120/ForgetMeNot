package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.LastTestedFilterDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDialogCaller
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterDialogState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class LastTestedFilterDialogStateProvider(
    json: Json,
    database: Database,
    override val key: String = LastTestedFilterDialogState::class.qualifiedName!!
) : BaseSerializableStateProvider<LastTestedFilterDialogState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val isFromDialog: Boolean,
        var isZeroTimeSelected: Boolean,
        val intervalInputValue: Int?,
        val intervalUnit: IntervalUnit,
        val caller: LastTestedFilterDialogCaller
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: LastTestedFilterDialogState) = SerializableState(
        state.isFromDialog,
        state.isZeroTimeSelected,
        state.timeAgo.value,
        state.timeAgo.intervalUnit,
        state.caller
    )

    override fun toOriginal(serializableState: SerializableState): LastTestedFilterDialogState {
        val timeAgo = DisplayedInterval(
            serializableState.intervalInputValue,
            serializableState.intervalUnit
        )
        return LastTestedFilterDialogState(
            serializableState.isFromDialog,
            serializableState.isZeroTimeSelected,
            timeAgo,
            serializableState.caller
        )
    }
}