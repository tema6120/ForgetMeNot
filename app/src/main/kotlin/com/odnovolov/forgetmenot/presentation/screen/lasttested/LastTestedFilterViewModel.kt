package com.odnovolov.forgetmenot.presentation.screen.lasttested

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LastTestedFilterViewModel(
    private val dialogState: LastTestedFilterDialogState
) {
    val isFromDialog: Boolean
        get() = dialogState.isFromDialog

    val intervalValueText: String
        get() = dialogState.timeAgo.value?.toString() ?: ""

    val displayedIntervalUnit: DisplayedInterval.IntervalUnit
        get() = dialogState.timeAgo.intervalUnit

    val isZeroTimeSelected: Flow<Boolean> =
        dialogState.flowOf(LastTestedFilterDialogState::isZeroTimeSelected)

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isZeroTimeSelected,
        dialogState.timeAgo.asFlow()
    ) { isZeroTimeSelected: Boolean, timeAgo: DisplayedInterval ->
        isZeroTimeSelected || timeAgo.isValid()
    }
}