package com.odnovolov.forgetmenot.ui.exercise

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.common.ViewModel
import com.odnovolov.forgetmenot.entity.ExerciseCard
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.*

interface ExerciseViewModel : ViewModel<State, Action, Event> {

    data class State(
        val exerciseCards: LiveData<List<ExerciseCard>>,
        val isCurrentCardLearned: LiveData<Boolean>
    )

    sealed class Action {
        object MoveToNextPosition : Action()
    }

    sealed class Event {
        data class NewPageBecomesSelected(val position: Int) : Event()
        object ShowAnswerButtonClick : Event()
        object NotAskButtonClick : Event()
        object UndoButtonClick : Event()
    }

}