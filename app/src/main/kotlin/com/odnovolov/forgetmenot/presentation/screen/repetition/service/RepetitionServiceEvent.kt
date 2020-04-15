package com.odnovolov.forgetmenot.presentation.screen.repetition.service

sealed class RepetitionServiceEvent {
    object PauseNotificationActionClicked : RepetitionServiceEvent()
    object ResumeNotificationActionClicked : RepetitionServiceEvent()
}