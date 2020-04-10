package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class ManualTestExerciseCardController(
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

    fun onRememberButtonClicked() {
        exercise.answer(Remember)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onNotRememberButtonClicked() {
        exercise.answer(NotRemember)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onHintSelectionChanged(startIndex: Int, endIndex: Int) {
        exercise.setHintSelection(startIndex, endIndex)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }
}