package com.odnovolov.forgetmenot.presentation.screen.repetition.view.repetitioncard

sealed class RepetitionCardEvent {
    object ShowQuestionButtonClicked : RepetitionCardEvent()
    object ShowAnswerButtonClicked : RepetitionCardEvent()
}