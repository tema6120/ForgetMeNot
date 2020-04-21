package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Show
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardEvent.*

class OffTestExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver,
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
                exercise.setHintSelection(event.startIndex, event.endIndex)
            }

            is AnswerTextSelectionChanged -> {
                exercise.setAnswerSelection(event.selection)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        exerciseStateProvider.save(exercise.state)
    }
}