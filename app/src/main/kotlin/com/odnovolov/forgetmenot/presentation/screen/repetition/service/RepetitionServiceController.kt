package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider

class RepetitionServiceController(
    private val repetition: Repetition,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionStateProvider: UserSessionTermStateProvider<State>
) {
    fun onPauseNotificationActionClicked() {
        repetition.pause()
        longTermStateSaver.saveStateByRegistry()
        repetitionStateProvider.save(repetition.state)
    }

    fun onResumeNotificationActionClicked() {
        repetition.resume()
        longTermStateSaver.saveStateByRegistry()
    }
}