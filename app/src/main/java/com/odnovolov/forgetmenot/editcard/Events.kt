package com.odnovolov.forgetmenot.editcard

sealed class EditCardEvent {
    class QuestionInputChanged(val text: CharSequence?) : EditCardEvent()
    class AnswerInputChanged(val text: CharSequence?) : EditCardEvent()
    object ReverseCardButtonClicked : EditCardEvent()
    object CancelButtonClicked : EditCardEvent()
    object DoneButtonClicked : EditCardEvent()
}