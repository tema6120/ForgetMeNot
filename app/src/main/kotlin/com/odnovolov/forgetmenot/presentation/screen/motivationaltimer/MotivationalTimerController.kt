package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerController.Command
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerController.Command.*
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
        object AskUserToSaveChanges : Command()
    }

    override fun handle(event: MotivationalTimerEvent) {
        when (event) {
            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromMotivationalTimer {
                    val screenState = HelpArticleScreenState(HelpArticle.MotivationalTimer)
                    HelpArticleDiScope.create(screenState)
                }
            }

            CloseTipButtonClicked -> {
                screenState.tip?.state?.needToShow = false
                screenState.tip = null
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

            BackButtonClicked -> {
                val isUserInputValid =
                    !screenState.isTimerEnabled || screenState.timeInput.toIntOrNull() ?: -1 > 0
                if (!isUserInputValid) {
                    navigator.navigateUp()
                    return
                }
                val newTimeForAnswer: Int = when {
                    !screenState.isTimerEnabled -> NOT_TO_USE_TIMER
                    else -> screenState.timeInput.toIntOrNull()!!
                }
                val currentTimeForAnswer: Int =
                    deckSettings.state.deck.exercisePreference.timeForAnswer
                if (newTimeForAnswer != currentTimeForAnswer) {
                    sendCommand(AskUserToSaveChanges)
                } else {
                    navigator.navigateUp()
                }
            }

            SaveButtonClicked -> {
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
                navigator.navigateUp()
            }

            QuitButtonClicked -> {
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}