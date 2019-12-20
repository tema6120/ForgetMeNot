package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.AnswerController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestEvent.AnswerTextSelectionChanged
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestEvent.VariantSelected

class AnswerQuizTestController(id: Long) : AnswerController<AnswerQuizTestEvent, Nothing>(id) {
    override fun handleEvent(event: AnswerQuizTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            is VariantSelected -> {
                queries.setSelectedVariant(event.variantNumber, id)
                if (queries.isSelectedVariantCorrect(id).executeAsOne().asBoolean()) {
                    onCorrectAnswer()
                } else {
                    onWrongAnswer()
                    QuizComposer.composeWhereItNeeds()
                }
            }
        }
    }
}