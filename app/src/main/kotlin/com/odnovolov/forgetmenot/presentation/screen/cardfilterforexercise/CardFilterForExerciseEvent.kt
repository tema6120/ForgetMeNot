package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

sealed class CardFilterForExerciseEvent {
    object LimitButtonClicked : CardFilterForExerciseEvent()
    class GradeRangeChanged(val gradeRange: IntRange) : CardFilterForExerciseEvent()
    object LastTestedFromButtonClicked : CardFilterForExerciseEvent()
    object LastTestedToButtonClicked : CardFilterForExerciseEvent()
    object StartExerciseButtonClicked : CardFilterForExerciseEvent()
}