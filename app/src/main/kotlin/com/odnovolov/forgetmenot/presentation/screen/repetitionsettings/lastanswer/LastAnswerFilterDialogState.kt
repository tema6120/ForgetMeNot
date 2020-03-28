package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval

class LastAnswerFilterDialogState(
    isFromDialog: Boolean,
    isZeroTimeSelected: Boolean,
    timeAgo: DisplayedInterval
): FlowableState<LastAnswerFilterDialogState>() {
    val isFromDialog: Boolean by me(isFromDialog)
    var isZeroTimeSelected: Boolean by me(isZeroTimeSelected)
    val timeAgo: DisplayedInterval by me(timeAgo)
}