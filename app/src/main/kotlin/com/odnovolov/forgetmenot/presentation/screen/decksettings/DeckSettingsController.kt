package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer.MotivationalTimerDialogState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanDiScope

class DeckSettingsController(
    private val deckSettings: DeckSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckSettingsEvent, Nothing>() {
    private val currentExercisePreference get() = deckSettings.state.deck.exercisePreference

    override fun handle(event: DeckSettingsEvent) {
        when (event) {
            RandomOrderSwitchToggled -> {
                val newRandomOrder = !currentExercisePreference.randomOrder
                deckSettings.setRandomOrder(newRandomOrder)
            }

            is SelectedTestMethod -> {
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
            }

            is SelectedCardReverse -> {
                deckSettings.setCardReverse(event.cardReverse)
            }

            SpeakPlanButtonClicked -> {
                navigator.navigateToSpeakPlan {
                    SpeakPlanDiScope.create(PresetDialogState(), SpeakEventDialogState())
                }
            }

            TimeForAnswerButtonClicked -> {
                navigator.showMotivationalTimerDialog {
                    val timeForAnswer: Int = currentExercisePreference.timeForAnswer
                    val dialogState = MotivationalTimerDialogState(
                        isTimerEnabled = timeForAnswer != 0,
                        timeInput = if (timeForAnswer == 0) "15" else timeForAnswer.toString()
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