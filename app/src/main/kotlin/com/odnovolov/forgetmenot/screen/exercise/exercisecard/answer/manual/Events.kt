package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

sealed class AnswerManualTestEvent {
    class AnswerTextSelectionChanged(val selection: String) : AnswerManualTestEvent()
    class HintSelectionChanged(val startIndex: Int, val endIndex: Int) : AnswerManualTestEvent()
    object RememberButtonClicked : AnswerManualTestEvent()
    object NotRememberButtonClicked : AnswerManualTestEvent()
}