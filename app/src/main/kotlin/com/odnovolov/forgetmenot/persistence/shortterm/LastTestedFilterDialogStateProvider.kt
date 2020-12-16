package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.LastTestedFilterDialogStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.lasttested.LastTestedFilterDialogState
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
        val intervalUnit: IntervalUnit
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: LastTestedFilterDialogState) = SerializableState(
        state.isFromDialog,
        state.isZeroTimeSelected,
        state.timeAgo.value,
        state.timeAgo.intervalUnit
    )

    override fun toOriginal(serializableState: SerializableState): LastTestedFilterDialogState {
        val timeAgo = DisplayedInterval(
            serializableState.intervalInputValue,
            serializableState.intervalUnit
        )
        return LastTestedFilterDialogState(
            serializableState.isFromDialog,
            serializableState.isZeroTimeSelected,
            timeAgo
        )
    }
}