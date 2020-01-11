package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

sealed class AnswerOffTestEvent {
    class AnswerTextSelectionChanged(val selection: String) : AnswerOffTestEvent()
    class HintSelectionChanged(val startIndex: Int, val endIndex: Int) : AnswerOffTestEvent()
    object ShowAnswerButtonClicked : AnswerOffTestEvent()
}