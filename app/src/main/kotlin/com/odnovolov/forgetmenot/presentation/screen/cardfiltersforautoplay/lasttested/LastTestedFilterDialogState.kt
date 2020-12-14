package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.lasttested

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval

class LastTestedFilterDialogState(
    isFromDialog: Boolean,
    isZeroTimeSelected: Boolean,
    timeAgo: DisplayedInterval
): FlowMaker<LastTestedFilterDialogState>() {
    val isFromDialog: Boolean by flowMaker(isFromDialog)
    var isZeroTimeSelected: Boolean by flowMaker(isZeroTimeSelected)
    val timeAgo: DisplayedInterval by flowMaker(timeAgo)
}