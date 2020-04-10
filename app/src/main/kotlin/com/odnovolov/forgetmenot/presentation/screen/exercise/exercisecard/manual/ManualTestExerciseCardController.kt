package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.NotRemember
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Remember
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardEvent.*

class ManualTestExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<ManualTestExerciseCardEvent, Nothing>() {
    override fun handle(event: ManualTestExerciseCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
            }
            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }
            RememberButtonClicked -> {
                exercise.answer(Remember)
            }
            NotRememberButtonClicked -> {
                exercise.answer(NotRemember)
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
    }
}