package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.domain.entity.NOT_TO_USE_TIMER
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.motivationaltimer.MotivationalTimerDialogState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanDiScope
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.testingmethod.TestingMethodDiScope

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

            IntervalsButtonClicked -> {
                navigator.navigateToIntervals {
                    IntervalsDiScope()
                }
            }

            PronunciationButtonClicked -> {
                navigator.navigateToPronunciation {
                    PronunciationDiScope()
                }
            }

            CardInversionButtonClicked -> {
                navigator.navigateToCardInversion {
                    CardInversionDiScope()
                }
            }

            QuestionDisplayButtonClicked -> {
                navigator.navigateToQuestionDisplay {
                    QuestionDisplayDiScope()
                }
            }

            TestingMethodButtonClicked -> {
                navigator.navigateToTestingMethod {
                    TestingMethodDiScope()
                }
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