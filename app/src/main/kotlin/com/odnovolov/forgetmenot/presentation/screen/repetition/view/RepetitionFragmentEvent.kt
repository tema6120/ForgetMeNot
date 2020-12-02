package com.odnovolov.forgetmenot.presentation.screen.repetition.view

sealed class RepetitionFragmentEvent {
    class NewPageBecameSelected(val position: Int) : RepetitionFragmentEvent()
    object GradeButtonClicked : RepetitionFragmentEvent()
    class GradeWasChanged(val grade: Int) : RepetitionFragmentEvent()
    object NotAskButtonClicked : RepetitionFragmentEvent()
    object AskAgainButtonClicked : RepetitionFragmentEvent()
    object SpeakButtonClicked : RepetitionFragmentEvent()
    object StopSpeakButtonClicked : RepetitionFragmentEvent()
    object EditCardButtonClicked : RepetitionFragmentEvent()
    object PauseButtonClicked : RepetitionFragmentEvent()
    object ResumeButtonClicked : RepetitionFragmentEvent()
    object SearchButtonClicked : RepetitionFragmentEvent()
    object HelpButtonClicked : RepetitionFragmentEvent()
}