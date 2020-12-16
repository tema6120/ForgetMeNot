package com.odnovolov.forgetmenot.presentation.screen.player.service

sealed class PlayerServiceEvent {
    object PauseNotificationActionClicked : PlayerServiceEvent()
    object ResumeNotificationActionClicked : PlayerServiceEvent()
}