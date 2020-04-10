package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

sealed class EntryTestExerciseCardEvent {
    object ShowQuestionButtonClicked : EntryTestExerciseCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : EntryTestExerciseCardEvent()
    class AnswerInputChanged(val text: String) : EntryTestExerciseCardEvent()
    class HintSelectionChanged(val startIndex: Int, val endIndex: Int) : EntryTestExerciseCardEvent()
    object CheckButtonClicked : EntryTestExerciseCardEvent()
    class AnswerTextSelectionChanged(val selection: String) : EntryTestExerciseCardEvent()
}