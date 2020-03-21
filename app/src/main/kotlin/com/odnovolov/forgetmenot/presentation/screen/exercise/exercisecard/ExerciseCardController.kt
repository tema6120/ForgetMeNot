package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class ExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) {
    fun onShowQuestionButtonClicked() {
        exercise.showQuestion()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onQuestionTextSelectionChanged(selection: String) {
        exercise.setQuestionSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }
}