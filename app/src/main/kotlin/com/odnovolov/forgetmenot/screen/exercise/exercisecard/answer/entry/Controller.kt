package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.BaseExerciseController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry.AnswerEntryTestEvent.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardControllerQueries

class AnswerEntryTestController(private val id: Long) : BaseExerciseController<AnswerEntryTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: AnswerEntryTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            is AnswerInputChanged -> {
                val answerInput: String = event.text?.toString() ?: ""
                queries.setAnswerInput(answerInput, id)
            }

            is HintSelectionChanged -> {
                queries.setHintSelectedRange(event.startIndex, event.endIndex)
            }

            CheckButtonClicked -> {
                if (queries.isAnswerInputCorrect(id).executeAsOne().asBoolean()) {
                    onCorrectAnswer(id)
                } else {
                    onWrongAnswer(id)
                    queries.createAnswerInputWhereItNeeds()
                }
            }
        }
    }
}