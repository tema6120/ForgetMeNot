package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.isAnswered
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

class ExerciseController(
    private val exercise: Exercise,
    private val walkingModePreference: WalkingModePreference,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val screenState: ExerciseScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val exerciseStateProvider: ShortTermStateProvider<Exercise.State>,
    private val exerciseScreenStateProvider: ShortTermStateProvider<ExerciseScreenState>
) : BaseController<ExerciseEvent, Command>() {
    sealed class Command {
        object MoveToNextPosition : Command()
        object MoveToPreviousPosition : Command()
        class MoveToPosition(val position: Int) : Command()
        object ShowQuitExerciseBottomSheet : Command()
    }

    private val currentExerciseCard: ExerciseCard?
        get() = with(exercise.state) {
            exerciseCards.getOrNull(currentPosition)
        }

    override fun handle(event: ExerciseEvent) {
        when (event) {
            is PageSelected -> {
                exercise.setCurrentPosition(event.position)
            }

            is GradeWasChanged -> {
                exercise.setGrade(event.grade)
            }

            MarkAsLearnedButtonClicked -> {
                exercise.setIsCardLearned(true)
            }

            MarkAsUnlearnedButtonClicked -> {
                exercise.setIsCardLearned(false)
            }

            SpeakButtonClicked -> {
                exercise.speak()
            }

            StopSpeakButtonClicked -> {
                exercise.stopSpeaking()
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

            EditDeckSettingsButtonClicked -> {
                val currentExerciseCard = currentExerciseCard ?: return
                exercise.stopSpeaking()
                screenState.wereDeckSettingsEdited = true
                navigator.navigateToDeckEditorFromExercise {
                    val deck: Deck = currentExerciseCard.base.deck
                    val tabs = DeckEditorTabs.OnlyDeckSettings
                    val screenState = DeckEditorScreenState(deck, tabs)
                    val batchCardEditor = BatchCardEditor(globalState, exercise = exercise)
                    DeckEditorDiScope.create(screenState, batchCardEditor)
                }
            }

            EditCardButtonClicked -> {
                val currentExerciseCard = currentExerciseCard ?: return
                exercise.stopSpeaking()
                navigator.navigateToCardsEditorFromExercise {
                    val editableCard = EditableCard(
                        currentExerciseCard.base.card,
                        currentExerciseCard.base.deck
                    )
                    val editableCards = listOf(editableCard)
                    val cardsEditorState = CardsEditor.State(editableCards)
                    val cardsEditor = CardsEditorForEditingSpecificCards(
                        cardsEditorState,
                        globalState,
                        exercise
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }

            SearchButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToSearchFromExercise {
                    val initialSearchText = with(exercise.state) {
                        when {
                            questionSelection.isNotEmpty() -> questionSelection
                            answerSelection.isNotEmpty() -> answerSelection
                            else -> ""
                        }
                    }
                    val cardsSearcher = CardsSearcher(globalState)
                    val batchCardEditor = BatchCardEditor(
                        globalState,
                        exercise = exercise
                    )
                    SearchDiScope.create(cardsSearcher, batchCardEditor, initialSearchText)
                }
            }

            WalkingModeSettingsButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToWalkingModeSettingsFromExercise()
            }

            WalkingModeHelpButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToHelpArticleFromExercise {
                    val screenState = HelpArticleScreenState(HelpArticle.WalkingMode)
                    HelpArticleDiScope.create(screenState)
                }
            }

            WalkingModeSwitchToggled -> {
                val enabled = !globalState.isWalkingModeEnabled
                exercise.setWalkingModeEnabled(enabled)
            }

            HelpButtonClicked -> {
                exercise.stopSpeaking()
                navigator.navigateToHelpArticleFromExercise {
                    val screenState = HelpArticleScreenState(HelpArticle.Exercise)
                    HelpArticleDiScope.create(screenState)
                }
            }

            FragmentResumed -> {
                if (screenState.wereDeckSettingsEdited) {
                    exercise.notifyExercisePreferenceChanged()
                    screenState.wereDeckSettingsEdited = false
                }
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

    private fun onKeyGestureDetected(event: KeyGestureDetected) {
        val keyGestureAction: KeyGestureAction =
            walkingModePreference.keyGestureMap[event.keyGesture] ?: return
        when (keyGestureAction) {
            NO_ACTION -> return
            MOVE_TO_NEXT_CARD -> sendCommand(MoveToNextPosition)
            MOVE_TO_PREVIOUS_CARD -> sendCommand(MoveToPreviousPosition)
            MARK_AS_REMEMBER -> exercise.answer(Remember)
            MARK_AS_NOT_REMEMBER -> exercise.answer(NotRemember)
            MARK_CARD_AS_LEARNED -> exercise.setIsCardLearned(true)
            SPEAK_QUESTION -> exercise.speakQuestion()
            SPEAK_ANSWER -> exercise.speakAnswer()
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        exerciseStateProvider.save(exercise.state)
        exerciseScreenStateProvider.save(screenState)
    }
}