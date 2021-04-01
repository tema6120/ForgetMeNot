package com.odnovolov.forgetmenot.presentation.screen.lasttested

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval

class LastTestedFilterDialogState(
    isFromDialog: Boolean,
    isZeroTimeSelected: Boolean,
    timeAgo: DisplayedInterval,
    caller: LastTestedFilterDialogCaller
): FlowMaker<LastTestedFilterDialogState>() {
    val isFromDialog: Boolean by flowMaker(isFromDialog)
    var isZeroTimeSelected: Boolean by flowMaker(isZeroTimeSelected)
    val timeAgo: DisplayedInterval by flowMaker(timeAgo)
    val caller: LastTestedFilterDialogCaller by flowMaker(caller)
}

enum class LastTestedFilterDialogCaller {
    CardFilterForAutoplay,
    CardFilterForExercise
}