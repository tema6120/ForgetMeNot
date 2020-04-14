package com.odnovolov.forgetmenot.presentation.screen.editcard

sealed class EditCardEvent {
    class QuestionInputChanged(val text: String) : EditCardEvent()
    class AnswerInputChanged(val text: String) : EditCardEvent()
    object ReverseCardButtonClicked : EditCardEvent()
    object CancelButtonClicked : EditCardEvent()
    object AcceptButtonClicked : EditCardEvent()
}