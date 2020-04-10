package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Entry
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class EntryTestExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) {
    private var answerInput: String? = null

    fun onShowQuestionButtonClicked() {
        exercise.showQuestion()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onQuestionTextSelectionChanged(selection: String) {
        exercise.setQuestionSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerInputChanged(text: String) {
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

    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }
}