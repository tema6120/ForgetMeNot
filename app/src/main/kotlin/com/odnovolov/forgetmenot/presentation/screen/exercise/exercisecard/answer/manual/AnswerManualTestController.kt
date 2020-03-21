package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class AnswerManualTestController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) {
    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onHintSelectionChanged(startIndex: Int, endIndex: Int) {
        exercise.setHintSelection(startIndex, endIndex)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onRememberButtonClicked() {
        exercise.answer(Remember)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onNotRememberButtonClicked() {
        exercise.answer(NotRemember)
        longTermStateSaver.saveStateByRegistry()
    }
}