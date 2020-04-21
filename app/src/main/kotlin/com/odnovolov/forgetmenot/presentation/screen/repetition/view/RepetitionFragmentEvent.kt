package com.odnovolov.forgetmenot.presentation.screen.repetition.view

sealed class RepetitionFragmentEvent {
    class NewPageBecameSelected(val position: Int) : RepetitionFragmentEvent()
    object NotAskButtonClicked : RepetitionFragmentEvent()
    object AskAgainButtonClicked : RepetitionFragmentEvent()
    object PauseButtonClicked : RepetitionFragmentEvent()
    object ResumeButtonClicked : RepetitionFragmentEvent()
}