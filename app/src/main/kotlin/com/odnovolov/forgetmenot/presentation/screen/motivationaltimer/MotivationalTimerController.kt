package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerController.Command
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerController.Command.ShowInvalidEntryError
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerController.Command.ShowSavedMessage
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.*

class MotivationalTimerController(
    private val deckSettings: DeckSettings,
    private val exercise: ExampleExercise,
    private val screenState: MotivationalTimerScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<MotivationalTimerScreenState>
) : BaseController<MotivationalTimerEvent, Command>() {
    sealed class Command {
        object ShowInvalidEntryError : Command()
        object ShowSavedMessage : Command()
    }

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
                val input: Int? = screenState.timeInput.toIntOrNull()
                val timeForAnswer: Int = when {
                    !screenState.isTimerEnabled -> NOT_TO_USE_TIMER
                    input != null && input > 0 -> input
                    else -> {
                        sendCommand(ShowInvalidEntryError)
                        return
                    }
                }
                deckSettings.setTimeForAnswer(timeForAnswer)
                sendCommand(ShowSavedMessage)
                exercise.notifyExercisePreferenceChanged()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}