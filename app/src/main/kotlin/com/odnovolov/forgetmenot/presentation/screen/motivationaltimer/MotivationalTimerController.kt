package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.*

class MotivationalTimerController(
    private val deckSettings: DeckSettings,
    private val screenState: MotivationalTimerScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<MotivationalTimerScreenState>
) : BaseController<MotivationalTimerEvent, Nothing>() {
    override fun handle(event: MotivationalTimerEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpFromMotivationalTimer {
                    HelpDiScope(HelpArticle.MotivationalTimer)
                }
            }

            TimeForAnswerSwitchToggled -> {
                screenState.isTimerEnabled = !screenState.isTimerEnabled
            }

            is TimeInputChanged -> {
                screenState.timeInput = event.text
            }

            OkButtonClicked -> {
                with(screenState) {
                    val input: Int? = timeInput.toIntOrNull()
                    val timeForAnswer: Int = when {
                        !isTimerEnabled -> NOT_TO_USE_TIMER
                        input != null && input > 0 -> input
                        else -> return
                    }
                    deckSettings.setTimeForAnswer(timeForAnswer)
                }
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}