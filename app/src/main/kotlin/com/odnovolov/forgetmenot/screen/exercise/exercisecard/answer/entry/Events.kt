package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

sealed class AnswerEntryTestEvent {
    class AnswerTextSelectionChanged(val selection: String) : AnswerEntryTestEvent()
    class AnswerInputChanged(val text: CharSequence?) : AnswerEntryTestEvent()
    object CheckButtonClicked : AnswerEntryTestEvent()
}