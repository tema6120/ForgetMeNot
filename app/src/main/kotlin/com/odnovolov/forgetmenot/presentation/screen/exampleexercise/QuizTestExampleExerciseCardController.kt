package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Variant
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent.*

class QuizTestExampleExerciseCardController(
    private val exercise: ExampleExercise,
    private val exerciseStateProvider: ShortTermStateProvider<State>
) : BaseController<QuizTestExerciseCardEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: QuizTestExerciseCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
                saveState()
            }

            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }

            is VariantSelected -> {
                exercise.answer(Variant(event.variantIndex))
                saveState()
            }

            is AnswerTextSelectionChanged -> {
                exercise.setAnswerSelection(event.selection)
            }
        }
    }

    override fun saveState() {
        exerciseStateProvider.save(exercise.state)
    }
}