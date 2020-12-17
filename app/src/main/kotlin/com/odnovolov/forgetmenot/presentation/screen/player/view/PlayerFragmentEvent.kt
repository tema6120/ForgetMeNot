package com.odnovolov.forgetmenot.presentation.screen.player.view

sealed class PlayerFragmentEvent {
    class NewPageBecameSelected(val position: Int) : PlayerFragmentEvent()
    object GradeButtonClicked : PlayerFragmentEvent()
    class GradeWasChanged(val grade: Int) : PlayerFragmentEvent()
    object NotAskButtonClicked : PlayerFragmentEvent()
    object AskAgainButtonClicked : PlayerFragmentEvent()
    object SpeakButtonClicked : PlayerFragmentEvent()
    object StopSpeakButtonClicked : PlayerFragmentEvent()
    object EditCardButtonClicked : PlayerFragmentEvent()
    object PauseButtonClicked : PlayerFragmentEvent()
    object ResumeButtonClicked : PlayerFragmentEvent()
    object SearchButtonClicked : PlayerFragmentEvent()
    object InfinitePlaybackSwitchToggled : PlayerFragmentEvent()
    object HelpButtonClicked : PlayerFragmentEvent()
    object PlayAgainButtonClicked : PlayerFragmentEvent()
    object EndButtonClicked : PlayerFragmentEvent()
}