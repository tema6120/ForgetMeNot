package com.odnovolov.forgetmenot.presentation.screen.changegrade

sealed class ChangeGradeEvent {
    class GradeWasSelected(val grade: Int) : ChangeGradeEvent()
}