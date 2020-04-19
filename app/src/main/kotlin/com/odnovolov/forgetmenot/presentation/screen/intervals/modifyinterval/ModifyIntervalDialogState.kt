package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import kotlinx.serialization.Serializable

class ModifyIntervalDialogState(
    val dialogPurpose: DialogPurpose,
    val displayedInterval: DisplayedInterval
)

@Serializable
sealed class DialogPurpose {
    @Serializable
    object ToAddNewInterval : DialogPurpose()
    @Serializable
    class ToChangeInterval(val targetLevelOfKnowledge: Int) : DialogPurpose()
}