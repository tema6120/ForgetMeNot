package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Show
import com.odnovolov.forgetmenot.presentation.common.Store

class AnswerOffTestController(
    private val exercise: Exercise,
    private val store: Store
) {
    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        store.saveStateByRegistry()
    }

    fun onHintSelectionChanged(startIndex: Int, endIndex: Int) {
        // todo
    }

    fun onShowAnswerButtonClicked() {
        exercise.answer(Show)
        store.saveStateByRegistry()
    }
}