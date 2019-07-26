package com.odnovolov.forgetmenot.ui.exercisecreator

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.common.ViewModel
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.*

interface ExerciseCreatorViewModel : ViewModel<State, Action, Event> {

    data class State(
        val isProcessing: LiveData<Boolean>
    )

    sealed class Action {
        object NavigateToExercise : Action()
    }

    sealed class Event {
        data class CreateExercise(val deck: Deck) : Event()
    }

}