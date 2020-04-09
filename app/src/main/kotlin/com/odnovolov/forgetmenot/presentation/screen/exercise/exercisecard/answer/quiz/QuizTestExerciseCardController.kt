package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Variant
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class QuizTestExerciseCardController(
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

    fun onVariantSelected(variantIndex: Int) {
        exercise.answer(Variant(variantIndex))
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAnswerTextSelectionChanged(selection: String) {
        exercise.setAnswerSelection(selection)
        longTermStateSaver.saveStateByRegistry()
    }
}