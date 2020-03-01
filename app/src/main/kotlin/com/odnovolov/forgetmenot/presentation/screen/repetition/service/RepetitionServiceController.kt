package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition

class RepetitionServiceController(
    private val repetition: Repetition
) {
    fun onPauseNotificationActionClicked() {
        repetition.pause()
    }

    fun onResumeNotificationActionClicked() {
        repetition.resume()
    }
}