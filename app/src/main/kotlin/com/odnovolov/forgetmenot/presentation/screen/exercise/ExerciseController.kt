package com.odnovolov.forgetmenot.presentation.screen.exercise

import com.odnovolov.forgetmenot.common.entity.KeyGesture
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCommand.*

class ExerciseController(
    private val exercise: Exercise,
    private val navigator: Navigator,
    private val store: Store
) {
    private var isFragmentRemoving = false
    private val commandFlow = EventFlow<ExerciseCommand>()
    val commands = commandFlow.get()

    fun onPageSelected(position: Int) {
        exercise.setCurrentPosition(position)
        store.saveStateByRegistry()
    }

    fun onSetCardLearnedButtonClicked() {
        exercise.setIsCardLearned(true)
        store.saveStateByRegistry()
    }

    fun onUndoButtonClicked() {
        exercise.setIsCardLearned(false)
        store.saveStateByRegistry()
    }

    fun onSpeakButtonClicked() {
        exercise.speak()
        store.saveStateByRegistry()
    }

    fun onEditCardButtonClicked() {
        // todo: prepare EditCard screen state
        navigator.navigateToEditCard()
    }

    fun onHintButtonClicked() {
        if (exercise.currentExerciseCard.base.hint == null) {
            commandFlow.send(ShowChooseHintPopup)
        } else {
            // todo
        }
    }

    fun onHintAsQuizButtonClicked() {

    }

    fun onMaskLettersButtonClicked() {

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
        // todo
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