package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class LastAnswerFilterViewModel(
    private val dialogState: LastAnswerFilterDialogState
) {
    val isFromDialog: Boolean = dialogState.isFromDialog

    val intervalValueText: String
        get() = dialogState.timeAgo.value?.toString() ?: ""

    val displayedIntervalUnit: DisplayedInterval.IntervalUnit
        get() = dialogState.timeAgo.intervalUnit

    val isZeroTimeSelected: Flow<Boolean> =
        dialogState.flowOf(LastAnswerFilterDialogState::isZeroTimeSelected)

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isZeroTimeSelected,
        dialogState.timeAgo.asFlow()
    ) { isZeroTimeSelected: Boolean, timeAgo: DisplayedInterval ->
        isZeroTimeSelected || timeAgo.isValid()
    }
}