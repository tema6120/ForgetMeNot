package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings

import com.odnovolov.forgetmenot.domain.entity.DO_NOT_USE_TIMER
import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExerciseStateCreator
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip.*
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingDiScope
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingScreenState
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsScreenState
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerDiScope
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerScreenState
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationEventDialogState
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanScreenState
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayScreenState
import com.odnovolov.forgetmenot.presentation.screen.testingmethod.TestingMethodDiScope
import com.odnovolov.forgetmenot.presentation.screen.testingmethod.TestingMethodScreenState

class DeckSettingsController(
    private val deckSettings: DeckSettings,
    private val exampleExerciseStateCreator: ExampleExerciseStateCreator,
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

            PronunciationButtonClicked -> {
                navigator.navigateToPronunciation(
                    createExampleExerciseDiScope = {
                        val exerciseState: Exercise.State =
                            exampleExerciseStateCreator.create(doNotInvert = true)
                        ExampleExerciseDiScope.create(exerciseState, useTimer = false)
                    },
                    createPronunciationDiScope = {
                        val possibleTips = listOf(
                            TipPronunciationScreenSelectLanguages,
                            TipPronunciationScreenAboutTTS,
                            TipPronunciationScreenAboutAutoSpeaking,
                            TipPronunciationScreenAboutSelection,
                            TipPronunciationScreenAboutBrackets
                        )
                        val tipToShow: Tip? = determineTipToShow(possibleTips)
                        val screenState = PronunciationScreenState(tipToShow)
                        PronunciationDiScope.create(screenState)
                    }
                )
            }

            CardInversionButtonClicked -> {
                navigator.navigateToCardInversion(
                    createExampleExerciseDiScope = {
                        val exerciseState: Exercise.State = exampleExerciseStateCreator.create()
                        ExampleExerciseDiScope.create(exerciseState, useTimer = false)
                    },
                    createCardInversionDiScope = {
                        val possibleTips = listOf(TipCardInversionScreen)
                        val tipToShow: Tip? = determineTipToShow(possibleTips)
                        val screenState = CardInversionScreenState(tipToShow)
                        CardInversionDiScope.create(screenState)
                    }
                )
            }

            QuestionDisplayButtonClicked -> {
                navigator.navigateToQuestionDisplay(
                    createExampleExerciseDiScope = {
                        val exerciseState: Exercise.State = exampleExerciseStateCreator.create()
                        ExampleExerciseDiScope.create(exerciseState, useTimer = false)
                    },
                    createQuestionDisplayDiScope = {
                        val possibleTips = listOf(TipQuestionDisplayScreenWhy)
                        val tipToShow: Tip? = determineTipToShow(possibleTips)
                        val screenState = QuestionDisplayScreenState(tipToShow)
                        QuestionDisplayDiScope.create(screenState)
                    }
                )
            }

            TestingMethodButtonClicked -> {
                navigator.navigateToTestingMethod(
                    createExampleExerciseDiScope = {
                        val exerciseState: Exercise.State = exampleExerciseStateCreator.create()
                        ExampleExerciseDiScope.create(exerciseState, useTimer = false)
                    },
                    createTestingMethodDiScope = {
                        val possibleTipToShow: Tip =
                            when (currentExercisePreference.testingMethod) {
                                TestingMethod.Off -> TipTestingMethodScreenWithoutTesting
                                TestingMethod.Manual -> TipTestingMethodScreenSelfTesting
                                TestingMethod.Quiz -> TipTestingMethodScreenTestingWithVariants
                                TestingMethod.Entry -> TipTestingMethodScreenSpellCheck
                            }
                        val tipToShow: Tip? =
                            if (possibleTipToShow.state.needToShow) possibleTipToShow else null
                        val screenState = TestingMethodScreenState(tipToShow)
                        TestingMethodDiScope.create(screenState)
                    }
                )
            }

            IntervalsButtonClicked -> {
                navigator.navigateToIntervals {
                    val possibleTips = listOf(
                        TipIntervalsScreenImportance,
                        TipIntervalsScreenAdjustIntervalScheme
                    )
                    val tipToShow: Tip? = determineTipToShow(possibleTips)
                    val screenState = IntervalsScreenState(tipToShow)
                    IntervalsDiScope.create(screenState)
                }
            }

            GradingButtonClicked -> {
                navigator.navigateToGrading {
                    val possibleTips =
                        if (currentExercisePreference.intervalScheme != null) {
                            listOf(
                                TipGradingScreenIndication,
                                TipGradingScreenAboutRelationshipWithIntervals,
                                TipGradingScreenIndicationAboutManualChange
                            )
                        } else {
                            listOf(
                                TipGradingScreenIndication,
                                TipGradingScreenIndicationAboutManualChange
                            )
                        }
                    val tipToShow: Tip? = determineTipToShow(possibleTips)
                    val screenState = GradingScreenState(tipToShow)
                    GradingDiScope.create(screenState)
                }
            }

            MotivationalTimerButtonClicked -> {
                navigator.navigateToMotivationalTimer(
                    createExampleExerciseDiScope = {
                        val exerciseState: Exercise.State = exampleExerciseStateCreator.create()
                        ExampleExerciseDiScope.create(exerciseState, useTimer = true)
                    },
                    createMotivationalTimerDiScope = {
                        val possibleTips = listOf(
                            TipMotivationalTimerScreenDescription,
                            TipMotivationalTimerScreenHowToDoWithThis
                        )
                        val tipToShow: Tip? = determineTipToShow(possibleTips)
                        val timeForAnswer = currentExercisePreference.timeForAnswer
                        val isTimerEnabled = timeForAnswer != DO_NOT_USE_TIMER
                        val timeInput: String =
                            if (timeForAnswer == DO_NOT_USE_TIMER) "15"
                            else timeForAnswer.toString()
                        val screenState = MotivationalTimerScreenState(
                            tipToShow,
                            isTimerEnabled,
                            timeInput
                        )
                        MotivationalTimerDiScope.create(screenState)
                    }
                )
            }

            PronunciationPlanButtonClicked -> {
                navigator.navigateToPronunciationPlan(
                    createExamplePlayerDiScope = ExamplePlayerDiScope::create,
                    createPronunciationPlanDiScope = {
                        val possibleTips = listOf(
                            TipPronunciationPlanScreenDescription,
                            TipPronunciationPlanScreenAboutLongerDelay,
                            TipPronunciationPlanScreenAboutRepetitionPronunciation
                        )
                        val tipToShow: Tip? = determineTipToShow(possibleTips)
                        val screenState = PronunciationPlanScreenState(tipToShow)
                        val dialogState = PronunciationEventDialogState()
                        PronunciationPlanDiScope.create(screenState, dialogState)
                    }
                )
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}