package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.AnswerController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry.AnswerEntryTestEvent.*

class AnswerEntryTestController(id: Long) : AnswerController<AnswerEntryTestEvent, Nothing>(id) {
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
                    onCorrectAnswer()
                } else {
                    onWrongAnswer()
                    queries.createAnswerInputWhereItNeeds()
                }
            }
        }
    }
}