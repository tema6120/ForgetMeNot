package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval

class LastAnswerFilterDialogState(
    isFromDialog: Boolean,
    isZeroTimeSelected: Boolean,
    timeAgo: DisplayedInterval
): FlowMaker<LastAnswerFilterDialogState>() {
    val isFromDialog: Boolean by flowMaker(isFromDialog)
    var isZeroTimeSelected: Boolean by flowMaker(isZeroTimeSelected)
    val timeAgo: DisplayedInterval by flowMaker(timeAgo)
}