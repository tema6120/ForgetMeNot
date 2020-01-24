package com.odnovolov.forgetmenot.screen.repetition.view

sealed class RepetitionViewEvent {
    class ShowAnswerButtonClicked(val id: Long) : RepetitionViewEvent()
}