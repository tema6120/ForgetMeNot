package com.odnovolov.forgetmenot.presentation.screen.changegrade

sealed class ChangeGradeEvent {
    class GradeSelected(val grade: Int) : ChangeGradeEvent()
}