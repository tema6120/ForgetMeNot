package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsController.Command
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsController.Command.ShowAutoSpeakOfQuestionIsOffMessage
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer.MotivationalTimerDialogState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanDiScope

class DeckSettingsController(
    private val deckSettings: DeckSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckSettingsEvent, Command>() {
    sealed class Command {
        object ShowAutoSpeakOfQuestionIsOffMessage : Command()
    }

    private val currentExercisePreference get() = deckSettings.state.deck.exercisePreference

    override fun handle(event: DeckSettingsEvent) {
        when (event) {
            RandomOrderSwitchToggled -> {
                val newRandomOrder = !currentExercisePreference.randomOrder
                deckSettings.setRandomOrder(newRandomOrder)
            }

            is TestMethodIsSelected -> {
                deckSettings.setTestMethod(event.testMethod)
            }

            IntervalsButtonClicked -> {
                navigator.navigateToIntervals {
                    IntervalsDiScope.create(PresetDialogState())
                }
            }

            PronunciationButtonClicked -> {
                navigator.navigateToPronunciation {
                    PronunciationDiScope.create(PresetDialogState())
                }
            }

            DisplayQuestionSwitchToggled -> {
                val newIsQuestionDisplayed = !currentExercisePreference.isQuestionDisplayed
                deckSettings.setIsQuestionDisplayed(newIsQuestionDisplayed)
                if (!newIsQuestionDisplayed
                    && !deckSettings.state.deck.exercisePreference.pronunciation.questionAutoSpeak
                ) {
                    sendCommand(ShowAutoSpeakOfQuestionIsOffMessage)
                }
            }

            is CardReverseIsSelected -> {
                deckSettings.setCardReverse(event.cardReverse)
            }

            PronunciationPlanButtonClicked -> {
                navigator.navigateToPronunciationPlan {
                    PronunciationPlanDiScope.create(
                        PresetDialogState(),
                        PronunciationEventDialogState()
                    )
                }
            }

            MotivationalTimerButtonClicked -> {
                navigator.showMotivationalTimerDialog {
                    val timeForAnswer = currentExercisePreference.timeForAnswer
                    val isTimerEnabled = timeForAnswer != NOT_TO_USE_TIMER
                    val timeInput: String =
                        if (timeForAnswer == NOT_TO_USE_TIMER) "15"
                        else timeForAnswer.toString()
                    val dialogState = MotivationalTimerDialogState(
                        isTimerEnabled,
                        timeInput
                    )
                    MotivationalTimerDiScope.create(dialogState)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}