package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForExercise
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.domain.interactor.exercise.isAnswered
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

class ExerciseController(
    private val exercise: Exercise,
    private val walkingModePreference: WalkingModePreference,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val exerciseStateProvider: ShortTermStateProvider<Exercise.State>
) : BaseController<ExerciseEvent, Command>() {
    sealed class Command {
        object MoveToNextPosition : Command()
        object MoveToPreviousPosition : Command()
        class MoveToPosition(val position: Int) : Command()
        class ShowIntervalsPopup(val intervalItems: List<IntervalItem>?) : Command()
        object ShowQuitExerciseBottomSheet : Command()
    }

    override fun handle(event: ExerciseEvent) {
        when (event) {
            is PageSelected -> {
                exercise.setCurrentPosition(event.position)
            }

            GradeButtonClicked -> {
                onGradeButtonClicked()
            }

            is GradeWasChanged -> {
                exercise.setGrade(event.grade)
            }

            NotAskButtonClicked -> {
                exercise.setIsCardLearned(true)
            }

            AskAgainButtonClicked -> {
                exercise.setIsCardLearned(false)
            }

            SpeakButtonClicked -> {
                exercise.speak()
            }

            StopSpeakButtonClicked -> {
                exercise.stopSpeaking()
            }

            EditCardButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToCardsEditorFromExercise {
                    val editableCard = EditableCard(
                        exercise.currentExerciseCard.base.card,
                        exercise.currentExerciseCard.base.deck
                    )
                    val editableCards = listOf(editableCard)
                    val cardsEditorState = CardsEditor.State(editableCards)
                    val cardsEditor = CardsEditorForExercise(
                        exercise,
                        state = cardsEditorState
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }

            StopTimerButtonClicked -> {
                exercise.stopTimer()
            }

            GetVariantsButtonClicked -> {
                exercise.getVariants()
            }

            MaskLettersButtonClicked -> {
                exercise.showHint()
            }

            SearchButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToSearchFromExercise {
                    val searchText = with(exercise.state) {
                        when {
                            questionSelection.isNotEmpty() -> questionSelection
                            answerSelection.isNotEmpty() -> answerSelection
                            else -> ""
                        }
                    }
                    SearchDiScope(searchText)
                }
            }

            WalkingModeSettingsButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToWalkingModeSettingsFromExercise()
            }

            WalkingModeHelpButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToHelpFromExercise {
                    HelpDiScope(HelpArticle.WalkingMode)
                }
            }

            WalkingModeSwitchToggled -> {
                val enabled = !globalState.isWalkingModeEnabled
                exercise.setWalkingModeEnabled(enabled)
            }

            HelpButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToHelpFromExercise {
                    HelpDiScope(HelpArticle.Exercise)
                }
            }

            FragmentResumed -> {
                exercise.startTimer()
            }

            FragmentPaused -> {
                exercise.resetTimer()
            }

            is KeyGestureDetected -> {
                onKeyGestureDetected(event)
            }

            BackButtonClicked -> {
                val unansweredCardCount = exercise.state.exerciseCards.count { exerciseCard ->
                    !exerciseCard.isAnswered && !exerciseCard.base.card.isLearned
                }
                if (unansweredCardCount > 0) {
                    sendCommand(ShowQuitExerciseBottomSheet)
                } else {
                    navigator.navigateUp()
                }
            }

            ShowUnansweredCardButtonClicked -> {
                val firstUnansweredCardPosition: Int =
                    exercise.state.exerciseCards.indexOfFirst { exerciseCard ->
                        !exerciseCard.isAnswered && !exerciseCard.base.card.isLearned
                    }
                if (firstUnansweredCardPosition >= 0) {
                    sendCommand(MoveToPosition(firstUnansweredCardPosition))
                }
            }

            UserConfirmedExit -> {
                navigator.navigateUp()
            }
        }
    }

    private fun onGradeButtonClicked() {
        val currentGrade: Int = exercise.currentExerciseCard.base.card.grade
        val intervalItems: List<IntervalItem>? = exercise.currentExerciseCard.base.deck
            .exercisePreference.intervalScheme?.intervals
            ?.map { interval: Interval ->
                IntervalItem(
                    grade = interval.grade,
                    waitingPeriod = interval.value,
                    isSelected = currentGrade == interval.grade
                )
            }
        sendCommand(ShowIntervalsPopup(intervalItems))
    }

    private fun onKeyGestureDetected(event: KeyGestureDetected) {
        val keyGestureAction: KeyGestureAction =
            walkingModePreference.keyGestureMap[event.keyGesture] ?: return
        when (keyGestureAction) {
            NO_ACTION -> return
            MOVE_TO_NEXT_CARD -> sendCommand(MoveToNextPosition)
            MOVE_TO_PREVIOUS_CARD -> sendCommand(MoveToPreviousPosition)
            MARK_AS_REMEMBER -> exercise.answer(Remember)
            MARK_AS_NOT_REMEMBER -> exercise.answer(NotRemember)
            MARK_CARD_AS_LEARNED -> {
                exercise.setIsCardLearned(true)
                sendCommand(MoveToNextPosition)
            }
            SPEAK_QUESTION -> exercise.speakQuestion()
            SPEAK_ANSWER -> exercise.speakAnswer()
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        exerciseStateProvider.save(exercise.state)
    }
}