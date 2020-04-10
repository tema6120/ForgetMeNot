package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off

sealed class OffTestExerciseCardEvent {
    object ShowQuestionButtonClicked : OffTestExerciseCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : OffTestExerciseCardEvent()
    object ShowAnswerButtonClicked : OffTestExerciseCardEvent()
    class HintSelectionChanged(val startIndex: Int, val endIndex: Int) : OffTestExerciseCardEvent()
    class AnswerTextSelectionChanged(val selection: String) : OffTestExerciseCardEvent()
}