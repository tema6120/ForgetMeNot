package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

sealed class AnswerManualTestEvent {
    class AnswerTextSelectionChanged(val selection: String) : AnswerManualTestEvent()
    object RememberButtonClicked : AnswerManualTestEvent()
    object NotRememberButtonClicked : AnswerManualTestEvent()
}