package com.odnovolov.forgetmenot.presentation.screen.changegrade

class ChangeGradeViewModel(
    private val dialogState: ChangeGradeDialogState
) {
    val gradeItems: List<GradeItem> get() = dialogState.gradeItems
}