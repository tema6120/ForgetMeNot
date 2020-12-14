package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay

sealed class CardFiltersForAutoplayEvent {
    object AvailableForExerciseCheckboxClicked : CardFiltersForAutoplayEvent()
    object AwaitingCheckboxClicked : CardFiltersForAutoplayEvent()
    object LearnedCheckboxClicked : CardFiltersForAutoplayEvent()
    class GradeRangeChanged(val gradeRange: IntRange) : CardFiltersForAutoplayEvent()
    object LastTestedFromButtonClicked : CardFiltersForAutoplayEvent()
    object LastTestedToButtonClicked : CardFiltersForAutoplayEvent()
    object StartPlayingButtonClicked : CardFiltersForAutoplayEvent()
}