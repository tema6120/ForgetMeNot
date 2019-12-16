package com.odnovolov.forgetmenot.screen.exercise.exercisecard.withouttest

sealed class ExerciseCardWithoutTestEvent {
    class QuestionTextSelectionChanged(val selection: String) : ExerciseCardWithoutTestEvent()
    class AnswerTextSelectionChanged(val selection: String) : ExerciseCardWithoutTestEvent()
    object ShowAnswerButtonClicked : ExerciseCardWithoutTestEvent()
    object ShowQuestionButtonClicked : ExerciseCardWithoutTestEvent()
}