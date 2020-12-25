package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer.MotivationalTimerEvent.*

class MotivationalTimerController(
    private val deckSettings: DeckSettings,
    private val dialogState: MotivationalTimerDialogState,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<MotivationalTimerDialogState>
) : BaseController<MotivationalTimerEvent, Nothing>() {
    override fun handle(event: MotivationalTimerEvent) {
        when (event) {
            TimeForAnswerSwitchToggled -> {
                dialogState.isTimerEnabled = !dialogState.isTimerEnabled
            }

            is TimeInputChanged -> {
                dialogState.timeInput = event.text
            }

            OkButtonClicked -> {
                with(dialogState) {
                    val input: Int? = timeInput.toIntOrNull()
                    val timeForAnswer: Int = when {
                        !isTimerEnabled -> 0
                        input != null && input > 0 -> input
                        else -> return
                    }
                    deckSettings.setTimeForAnswer(timeForAnswer)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}