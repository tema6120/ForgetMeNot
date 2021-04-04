package com.odnovolov.forgetmenot.presentation.screen.player.view

sealed class PlayerFragmentEvent {
    class PageWasChanged(val position: Int) : PlayerFragmentEvent()
    object GradeButtonClicked : PlayerFragmentEvent()
    class GradeWasSelected(val grade: Int) : PlayerFragmentEvent()
    object MarkAsLearnedButtonClicked : PlayerFragmentEvent()
    object MarkAsUnlearnedButtonClicked : PlayerFragmentEvent()
    object SpeakButtonClicked : PlayerFragmentEvent()
    object StopSpeakButtonClicked : PlayerFragmentEvent()
    object EditDeckSettingsButtonClicked : PlayerFragmentEvent()
    object EditCardButtonClicked : PlayerFragmentEvent()
    object SearchButtonClicked : PlayerFragmentEvent()
    object LapsButtonClicked : PlayerFragmentEvent()
    object HelpButtonClicked : PlayerFragmentEvent()
    object PauseButtonClicked : PlayerFragmentEvent()
    object ResumeButtonClicked : PlayerFragmentEvent()
    object FragmentResumed : PlayerFragmentEvent()
    object PlayAgainButtonClicked : PlayerFragmentEvent()
    object EndButtonClicked : PlayerFragmentEvent()
}