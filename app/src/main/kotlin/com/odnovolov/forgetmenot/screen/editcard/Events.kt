package com.odnovolov.forgetmenot.screen.editcard

sealed class EditCardEvent {
    class QuestionInputChanged(val text: CharSequence?) : EditCardEvent()
    class AnswerInputChanged(val text: CharSequence?) : EditCardEvent()
    object ReverseCardButtonClicked : EditCardEvent()
    object CancelButtonClicked : EditCardEvent()
    object DoneButtonClicked : EditCardEvent()
}