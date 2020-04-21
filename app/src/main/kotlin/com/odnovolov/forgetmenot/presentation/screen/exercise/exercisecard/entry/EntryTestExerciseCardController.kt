package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.Answer.Entry
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent.*

class EntryTestExerciseCardController(
    private val exercise: Exercise,
    private val longTermStateSaver: LongTermStateSaver,
    private val exerciseStateProvider: ShortTermStateProvider<State>
) : BaseController<EntryTestExerciseCardEvent, Nothing>() {
    private var answerInput: String? = null
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
                answerInput = event.text
            }

            is HintSelectionChanged -> {
                exercise.setHintSelection(event.startIndex, event.endIndex)
            }

            CheckButtonClicked -> {
                exercise.answer(Entry(answerInput))
                saveState()
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