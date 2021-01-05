package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Entry
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent.*

class EntryTestExampleExerciseCardController(
    private val exercise: ExampleExercise,
    private val exerciseStateProvider: ShortTermStateProvider<State>
) : BaseController<EntryTestExerciseCardEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: EntryTestExerciseCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
                saveState()
            }

            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }

            is AnswerInputChanged -> {
                exercise.setUserInput(event.text)
                saveState()
            }

            is HintSelectionChanged -> {

            }

            CheckButtonClicked -> {
                exercise.answer(Entry)
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