package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.AnswerController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestEvent.AnswerTextSelectionChanged
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestEvent.ShowAnswerButtonClicked

class AnswerOffTestController(id: Long) : AnswerController<AnswerOffTestEvent, Nothing>(id) {
    override fun handleEvent(event: AnswerOffTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            ShowAnswerButtonClicked -> {
                onCorrectAnswer()
            }
        }
    }
}