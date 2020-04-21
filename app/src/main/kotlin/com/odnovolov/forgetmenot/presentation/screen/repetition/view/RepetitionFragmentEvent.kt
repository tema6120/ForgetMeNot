package com.odnovolov.forgetmenot.presentation.screen.repetition.view

sealed class RepetitionFragmentEvent {
    class NewPageBecameSelected(val position: Int) : RepetitionFragmentEvent()
    object NotAskButtonClicked : RepetitionFragmentEvent()
    object AskAgainButtonClicked : RepetitionFragmentEvent()
    object SpeakButtonClicked : RepetitionFragmentEvent()
    object EditCardButtonClicked : RepetitionFragmentEvent()
    object StopSpeakButtonClicked : RepetitionFragmentEvent()
    object PauseButtonClicked : RepetitionFragmentEvent()
    object ResumeButtonClicked : RepetitionFragmentEvent()
}