package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval

class ModifyIntervalDialogState(
    val dialogPurpose: DialogPurpose,
    val grade: Int,
    val displayedInterval: DisplayedInterval
)

enum class DialogPurpose {
    ToAddNewInterval,
    ToChangeInterval
}