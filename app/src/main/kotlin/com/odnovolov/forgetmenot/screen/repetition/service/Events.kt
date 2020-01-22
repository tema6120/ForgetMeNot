package com.odnovolov.forgetmenot.screen.repetition.service

sealed class RepetitionServiceEvent {
    object Init : RepetitionServiceEvent()
    object SpeakingFinished : RepetitionServiceEvent()
    object DelayIsUp : RepetitionServiceEvent()
}