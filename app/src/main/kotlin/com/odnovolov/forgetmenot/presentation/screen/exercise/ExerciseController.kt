package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.editcard.EDIT_CARD_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardScreenState
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCommand.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import org.koin.java.KoinJavaComponent.getKoin

class ExerciseController(
    private val exercise: Exercise,
    private val walkingModePreference: WalkingModePreference,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val exerciseStateProvider: UserSessionTermStateProvider<Exercise.State>
) {
    private val commandFlow = EventFlow<ExerciseCommand>()
    val commands = commandFlow.get()

    init {
        longTermStateSaver.saveStateByRegistry()
    }

    fun onPageSelected(position: Int) {
        exercise.setCurrentPosition(position)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSetCardLearnedButtonClicked() {
        exercise.setIsCardLearned(true)
        longTermStateSaver.saveStateByRegistry()
        commandFlow.send(MoveToNextPosition)
    }

    fun onUndoButtonClicked() {
        exercise.setIsCardLearned(false)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onSpeakButtonClicked() {
        exercise.speak()
        longTermStateSaver.saveStateByRegistry()
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
            longTermStateSaver.saveStateByRegistry()
        }
    }

    fun onHintAsQuizButtonClicked() {
        exercise.hintAsQuiz()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onMaskLettersButtonClicked() {
        exercise.showHint()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLevelOfKnowledgeButtonClicked() {
        val intervalScheme: IntervalScheme? =
            exercise.currentExerciseCard.base.deck.exercisePreference.intervalScheme
        if (intervalScheme == null) {
            commandFlow.send(ShowIntervalsAreOffMessage)
        } else {
            val currentLevelOfKnowledge: Int =
                exercise.currentExerciseCard.base.card.levelOfKnowledge
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
        longTermStateSaver.saveStateByRegistry()
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
                longTermStateSaver.saveStateByRegistry()
            }
            SET_CARD_AS_NOT_REMEMBER -> {
                exercise.answer(NotRemember)
                longTermStateSaver.saveStateByRegistry()
            }
            SET_CARD_AS_LEARNED -> {
                exercise.setIsCardLearned(true)
                longTermStateSaver.saveStateByRegistry()
                commandFlow.send(MoveToNextPosition)
            }
            SPEAK_QUESTION -> {
                exercise.speakQuestion()
                longTermStateSaver.saveStateByRegistry()
            }
            SPEAK_ANSWER -> {
                exercise.speakAnswer()
                longTermStateSaver.saveStateByRegistry()
            }
        }
    }

    fun onFragmentPause() {
        exerciseStateProvider.save(exercise.state)
    }
}