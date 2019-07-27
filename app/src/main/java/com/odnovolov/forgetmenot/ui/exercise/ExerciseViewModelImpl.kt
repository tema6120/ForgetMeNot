package com.odnovolov.forgetmenot.ui.exercise

import androidx.lifecycle.*
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.entity.ExerciseCard
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.*
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.Action.MoveToNextPosition
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.Event.*

class ExerciseViewModelImpl(
    private val dao: ExerciseDao
) : ViewModel(), ExerciseViewModel {

    class Factory(val dao: ExerciseDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ExerciseViewModelImpl(dao) as T
        }
    }

    private val currentPosition = MutableLiveData<Int>()
    private val currentExerciseCard: ExerciseCard?
        get() {
            val exerciseCards = exerciseCards.value ?: return null
            val currentPosition = currentPosition.value ?: return null
            return exerciseCards[currentPosition]
        }

    private val exerciseCards: LiveData<List<ExerciseCard>> = dao.getExerciseCards()
    private val isCurrentCardLearned = MediatorLiveData<Boolean>().apply {
        fun updateValue() {
            value = currentExerciseCard?.card?.isLearned
        }

        addSource(currentPosition) { updateValue() }
        addSource(exerciseCards) { updateValue() }
    }

    override val state = State(
        exerciseCards,
        isCurrentCardLearned
    )

    private val actionSender = LiveEvent<Action>()
    override val action = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            is NewPageBecomesSelected -> {
                currentPosition.value = event.position
            }
            ShowAnswerButtonClick -> {
                dao.setAnswered(currentExerciseCard!!.id)
            }
            NotAskButtonClick -> {
                dao.setIsCardLearned(true, currentExerciseCard!!.card.id)
                if (!isLastPosition()) {
                    actionSender.send(MoveToNextPosition)
                }
            }
            UndoButtonClick -> {
                dao.setIsCardLearned(true, currentExerciseCard!!.card.id)
            }
        }
    }

    private fun isLastPosition(): Boolean {
        val lastPosition = exerciseCards.value!!.size - 1
        return lastPosition == currentPosition.value!!
    }

}