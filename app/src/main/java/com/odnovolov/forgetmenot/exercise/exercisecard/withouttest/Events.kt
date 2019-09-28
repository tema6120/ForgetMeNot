package com.odnovolov.forgetmenot.exercise.exercisecard.withouttest

sealed class ExerciseCardWithoutTestEvent {
    class QuestionTextSelectionChanged(val selection: String) : ExerciseCardWithoutTestEvent()
    class AnswerTextSelectionChanged(val selection: String) : ExerciseCardWithoutTestEvent()
    object ShowAnswerButtonClicked : ExerciseCardWithoutTestEvent()
}