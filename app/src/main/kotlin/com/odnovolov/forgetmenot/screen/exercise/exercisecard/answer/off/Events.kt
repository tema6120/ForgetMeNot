package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

sealed class AnswerOffTestEvent {
    class AnswerTextSelectionChanged(val selection: String) : AnswerOffTestEvent()
    object ShowAnswerButtonClicked : AnswerOffTestEvent()
}