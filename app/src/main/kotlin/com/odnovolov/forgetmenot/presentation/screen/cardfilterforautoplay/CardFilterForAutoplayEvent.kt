package com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay

sealed class CardFilterForAutoplayEvent {
    object AvailableForExerciseCheckboxClicked : CardFilterForAutoplayEvent()
    object AwaitingCheckboxClicked : CardFilterForAutoplayEvent()
    object LearnedCheckboxClicked : CardFilterForAutoplayEvent()
    class GradeRangeChanged(val gradeRange: IntRange) : CardFilterForAutoplayEvent()
    object LastTestedFromButtonClicked : CardFilterForAutoplayEvent()
    object LastTestedToButtonClicked : CardFilterForAutoplayEvent()
    object StartPlayingButtonClicked : CardFilterForAutoplayEvent()
}