package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Entry
import com.odnovolov.forgetmenot.presentation.common.Store

class AnswerEntryTestController(
    private val exercise: Exercise,
    private val store: Store
) {
    private var answerInput: String? = null

    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        store.saveStateByRegistry()
    }

    fun onAnswerInputChanged(text: String?) {
        answerInput = text
    }

    fun onHintSelectionChanged(startIndex: Int, endIndex: Int) {
        exercise.setHintSelection(startIndex, endIndex)
        store.saveStateByRegistry()
    }

    fun onCheckButtonClicked() {
        exercise.answer(Entry(answerInput))
        store.saveStateByRegistry()
    }
}