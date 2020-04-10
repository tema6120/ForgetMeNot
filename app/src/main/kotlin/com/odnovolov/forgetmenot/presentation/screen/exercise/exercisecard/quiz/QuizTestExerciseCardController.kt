package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Variant
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent.*

class QuizTestExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<QuizTestExerciseCardEvent, Nothing>() {
    override fun handle(event: QuizTestExerciseCardEvent) {
        when(event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
            }
            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }
            is VariantSelected -> {
                exercise.answer(Variant(event.variantIndex))
            }
            is AnswerTextSelectionChanged -> {
                exercise.setAnswerSelection(event.selection)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}