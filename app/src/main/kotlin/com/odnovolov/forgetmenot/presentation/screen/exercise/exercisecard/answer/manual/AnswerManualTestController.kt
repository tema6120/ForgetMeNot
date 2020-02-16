package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.Store

class AnswerManualTestController(
    private val exercise: Exercise,
    private val store: Store
) {
    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        store.saveStateByRegistry()
    }

    fun onHintSelectionChanged(startIndex: Int, endIndex: Int) {
        exercise.setHintSelection(startIndex, endIndex)
        store.saveStateByRegistry()
    }

    fun onRememberButtonClicked() {
        exercise.answer(Remember)
        store.saveStateByRegistry()
    }

    fun onNotRememberButtonClicked() {
        exercise.answer(NotRemember)
        store.saveStateByRegistry()
    }
}