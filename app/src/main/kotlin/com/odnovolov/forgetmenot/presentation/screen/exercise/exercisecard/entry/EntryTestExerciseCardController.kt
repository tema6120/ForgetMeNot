package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Entry
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent.*

class EntryTestExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<EntryTestExerciseCardEvent, Nothing>() {
    private var answerInput: String? = null

    override fun handle(event: EntryTestExerciseCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                exercise.showQuestion()
            }
            is QuestionTextSelectionChanged -> {
                exercise.setQuestionSelection(event.selection)
            }
            is AnswerInputChanged -> {
                answerInput = event.text
            }
            is HintSelectionChanged -> {
                exercise.setHintSelection(event.startIndex, event.endIndex)
            }
            CheckButtonClicked -> {
                exercise.answer(Entry(answerInput))
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