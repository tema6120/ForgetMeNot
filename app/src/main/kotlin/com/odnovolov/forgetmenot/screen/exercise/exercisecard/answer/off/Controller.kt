package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.AnswerController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestEvent.*

class AnswerOffTestController(id: Long) : AnswerController<AnswerOffTestEvent, Nothing>(id) {
    override fun handleEvent(event: AnswerOffTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            is HintSelectionChanged -> {
                queries.setHintSelectedRange(event.startIndex, event.endIndex)
            }

            ShowAnswerButtonClicked -> {
                onCorrectAnswer()
            }
        }
    }
}