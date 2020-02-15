package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.common.Store

class ExerciseCardController(
    private val exercise: Exercise,
    private val store: Store
) {
    fun onShowQuestionButtonClicked() {
        exercise.showQuestion()
        store.saveStateByRegistry()
    }

    fun onQuestionTextSelectionChanged(selection: String) {
        exercise.setQuestionSelection(selection)
        store.saveStateByRegistry()
    }
}