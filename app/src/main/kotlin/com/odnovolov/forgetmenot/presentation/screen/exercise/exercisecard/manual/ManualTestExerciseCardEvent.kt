package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual

sealed class ManualTestExerciseCardEvent {
    object ShowQuestionButtonClicked : ManualTestExerciseCardEvent()
    class QuestionTextSelectionChanged(val selection: String) : ManualTestExerciseCardEvent()
    object RememberButtonClicked : ManualTestExerciseCardEvent()
    object NotRememberButtonClicked : ManualTestExerciseCardEvent()
    class HintSelectionChanged(val startIndex: Int, val endIndex: Int) : ManualTestExerciseCardEvent()
    class AnswerTextSelectionChanged(val selection: String) : ManualTestExerciseCardEvent()
}