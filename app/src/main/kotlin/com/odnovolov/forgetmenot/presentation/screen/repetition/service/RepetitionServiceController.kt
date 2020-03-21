package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition.State
import com.odnovolov.forgetmenot.presentation.common.StateProvider

class RepetitionServiceController(
    private val repetition: Repetition,
    private val repetitionStateProvider: StateProvider<State>
) {
    fun onPauseNotificationActionClicked() {
        repetition.pause()
        repetitionStateProvider.save(repetition.state)
    }

    fun onResumeNotificationActionClicked() {
        repetition.resume()
    }
}