package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Entry
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class AnswerEntryTestController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) {
    private var answerInput: String? = null

    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerInputChanged(text: String?) {
        answerInput = text
    }

    fun onHintSelectionChanged(startIndex: Int, endIndex: Int) {
        exercise.setHintSelection(startIndex, endIndex)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onCheckButtonClicked() {
        exercise.answer(Entry(answerInput))
        longTermStateSaver.saveStateByRegistry()
    }
}