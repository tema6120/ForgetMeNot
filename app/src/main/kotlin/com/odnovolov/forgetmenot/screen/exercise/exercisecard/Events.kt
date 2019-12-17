package com.odnovolov.forgetmenot.screen.exercise.exercisecard

sealed class ExerciseCardEvent {
    object ShowQuestionButtonClicked : ExerciseCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : ExerciseCardEvent()
}