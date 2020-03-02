package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.editcard.EDIT_CARD_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardScreenState
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCommand.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import org.koin.core.KoinComponent

class ExerciseController(
    private val exercise: Exercise,
    private val walkingModePreference: WalkingModePreference,
    private val navigator: Navigator,
    private val store: Store
) : KoinComponent {
    private var isFragmentRemoving = false
    private val commandFlow = EventFlow<ExerciseCommand>()
    val commands = commandFlow.get()

    init {
        store.saveStateByRegistry()
    }

    fun onPageSelected(position: Int) {
        exercise.setCurrentPosition(position)
        store.saveStateByRegistry()
    }

    fun onSetCardLearnedButtonClicked() {
        exercise.setIsCardLearned(true)
        store.saveStateByRegistry()
        commandFlow.send(MoveToNextPosition)
    }

    fun onUndoButtonClicked() {
        exercise.setIsCardLearned(false)
        store.saveStateByRegistry()
    }

    fun onSpeakButtonClicked() {
        exercise.speak()
    }

    fun onEditCardButtonClicked() {
        val editCardScreenState = EditCardScreenState().apply {
            question = exercise.currentExerciseCard.base.card.question
            answer = exercise.currentExerciseCard.base.card.answer
        }
        val koinScope = getKoin().createScope<EditCardViewModel>(EDIT_CARD_SCOPE_ID)
        koinScope.declare(editCardScreenState, override = true)
        navigator.navigateToEditCard()
    }

    fun onHintButtonClicked() {
        if (exercise.currentExerciseCard.base.hint == null) {
            commandFlow.send(ShowChooseHintPopup)
        } else {
            exercise.showHint()
            store.saveStateByRegistry()
        }
    }

    fun onHintAsQuizButtonClicked() {
        exercise.hintAsQuiz()
        store.saveStateByRegistry()
    }

    fun onMaskLettersButtonClicked() {
        exercise.showHint()
        store.saveStateByRegistry()
    }

    fun onLevelOfKnowledgeButtonClicked() {
        val intervalScheme = exercise.currentExerciseCard.base.deck.exercisePreference.intervalScheme
        if (intervalScheme == null) {
            commandFlow.send(ShowIntervalsAreOffMessage)
        } else {
            val currentLevelOfKnowledge: Int = exercise.currentExerciseCard.base.card.levelOfKnowledge
            val intervalItems: List<IntervalItem> = intervalScheme.intervals
                .map { interval: Interval ->
                    IntervalItem(
                        levelOfKnowledge = interval.targetLevelOfKnowledge - 1,
                        waitingPeriod = interval.value,
                        isSelected = currentLevelOfKnowledge == interval.targetLevelOfKnowledge - 1
                    )
                }
            commandFlow.send(ShowLevelOfKnowledgePopup(intervalItems))
        }
    }

    fun onLevelOfKnowledgeSelected(levelOfKnowledge: Int) {
        exercise.setLevelOfKnowledge(levelOfKnowledge)
        store.saveStateByRegistry()
    }

    fun onKeyGestureDetected(keyGesture: KeyGesture) {
        val keyGestureAction: KeyGestureAction =
            walkingModePreference.keyGestureMap[keyGesture] ?: return
        when (keyGestureAction) {
            NO_ACTION -> return
            MOVE_TO_NEXT_CARD -> commandFlow.send(MoveToNextPosition)
            MOVE_TO_PREVIOUS_CARD -> commandFlow.send(MoveToPreviousPosition)
            SET_CARD_AS_REMEMBER -> {
                exercise.answer(Remember)
                store.saveStateByRegistry()
            }
            SET_CARD_AS_NOT_REMEMBER -> {
                exercise.answer(NotRemember)
                store.saveStateByRegistry()
            }
            SET_CARD_AS_LEARNED -> {
                exercise.setIsCardLearned(true)
                store.saveStateByRegistry()
                commandFlow.send(MoveToNextPosition)
            }
            SPEAK_QUESTION -> exercise.speakQuestion()
            SPEAK_ANSWER -> exercise.speakAnswer()
        }
    }

    fun onFragmentRemoving() {
        isFragmentRemoving = true
    }

    fun onCleared() {
        if (isFragmentRemoving) {
            store.deleteExerciseState()
        } else {
            store.save(exercise.state)
        }
    }
}