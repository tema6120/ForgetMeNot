package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Show
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardEvent.*

class OffTestExampleExerciseCardController(
    private val exercise: ExampleExercise,
    private val exerciseStateProvider: ShortTermStateProvider<State>
) : BaseController<OffTestExerciseCardEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: OffTestExerciseCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
                saveState()
            }

            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }

            ShowAnswerButtonClicked -> {
                exercise.answer(Show)
                saveState()
            }

            is HintSelectionChanged -> {

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