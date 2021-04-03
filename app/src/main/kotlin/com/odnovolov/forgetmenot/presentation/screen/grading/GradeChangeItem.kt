package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.entity.GradeChange
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item

data class GradeChangeItem(
    val gradeChange: GradeChange,
    override val text: String,
    override val isSelected: Boolean
) : Item