package com.odnovolov.forgetmenot.presentation.screen.repetition.service

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionServiceEvent.PauseNotificationActionClicked
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionServiceEvent.ResumeNotificationActionClicked

class RepetitionServiceController(
    private val repetition: Repetition,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionStateProvider: ShortTermStateProvider<State>
) : BaseController<RepetitionServiceEvent, Nothing>() {
    override fun handle(event: RepetitionServiceEvent) {
        when (event) {
            PauseNotificationActionClicked -> {
                repetition.pause()
            }

            ResumeNotificationActionClicked -> {
                repetition.resume()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        repetitionStateProvider.save(repetition.state)
    }
}