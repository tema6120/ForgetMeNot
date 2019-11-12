package com.odnovolov.forgetmenot.screen.exercise.exercisecard.manualtestmethod

sealed class ExerciseCardManualTestMethodEvent {
    class QuestionTextSelectionChanged(val selection: String) : ExerciseCardManualTestMethodEvent()
    class AnswerTextSelectionChanged(val selection: String) : ExerciseCardManualTestMethodEvent()
    object RememberButtonClicked : ExerciseCardManualTestMethodEvent()
    object NotRememberButtonClicked : ExerciseCardManualTestMethodEvent()
}