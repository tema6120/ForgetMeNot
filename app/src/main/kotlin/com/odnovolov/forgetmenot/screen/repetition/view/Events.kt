package com.odnovolov.forgetmenot.screen.repetition.view

sealed class RepetitionViewEvent {
    class NewPageBecameSelected(val id: Long) : RepetitionViewEvent()
    class ShowAnswerButtonClicked(val id: Long) : RepetitionViewEvent()
}