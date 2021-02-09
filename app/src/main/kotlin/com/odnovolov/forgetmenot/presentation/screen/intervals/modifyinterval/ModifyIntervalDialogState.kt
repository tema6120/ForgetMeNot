package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval

class ModifyIntervalDialogState(
    val purpose: Purpose,
    val grade: Int,
    val displayedInterval: DisplayedInterval
) {
    enum class Purpose {
        ToAddNewInterval,
        ToChangeInterval
    }
}