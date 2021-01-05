package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardEvent.*

class ManualTestExampleExerciseCardController(
    private val exercise: ExampleExercise,
    private val exerciseStateProvider: ShortTermStateProvider<State>
) : BaseController<ManualTestExerciseCardEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: ManualTestExerciseCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
                saveState()
            }

            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }

            RememberButtonClicked -> {
                exercise.answer(Remember)
                saveState()
            }

            NotRememberButtonClicked -> {
                exercise.answer(NotRemember)
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