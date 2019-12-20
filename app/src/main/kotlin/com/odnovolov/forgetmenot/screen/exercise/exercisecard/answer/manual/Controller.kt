package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.AnswerController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual.AnswerManualTestEvent.*

class AnswerManualTestController(id: Long) : AnswerController<AnswerManualTestEvent, Nothing>(id) {
    override fun handleEvent(event: AnswerManualTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            RememberButtonClicked -> {
                onCorrectAnswer()
            }

            NotRememberButtonClicked -> {
                onWrongAnswer()
            }
        }
    }
}