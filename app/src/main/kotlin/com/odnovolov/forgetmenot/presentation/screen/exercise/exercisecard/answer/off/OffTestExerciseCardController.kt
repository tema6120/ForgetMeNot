package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Show
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class OffTestExerciseCardController(
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

    fun onShowAnswerButtonClicked() {
        exercise.answer(Show)
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