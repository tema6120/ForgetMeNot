package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ModifyIntervalViewModel(
    private val modifyIntervalDialogState: ModifyIntervalDialogState
) {
    val intervalValueText: String
        get() = modifyIntervalDialogState.displayedInterval.value?.toString() ?: ""

    val displayedIntervalUnit: DisplayedInterval.IntervalUnit
        get() = modifyIntervalDialogState.displayedInterval.intervalUnit

    val isOkButtonEnabled: Flow<Boolean> = modifyIntervalDialogState.displayedInterval.asFlow()
        .map { it.isValid() }
}