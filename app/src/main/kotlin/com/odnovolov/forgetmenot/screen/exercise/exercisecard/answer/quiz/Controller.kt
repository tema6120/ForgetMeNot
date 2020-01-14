package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz

import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.BaseExerciseController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestEvent.AnswerTextSelectionChanged
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestEvent.VariantSelected
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestOrder.Vibrate
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardControllerQueries

class AnswerQuizTestController(private val id: Long) :
    BaseExerciseController<AnswerQuizTestEvent, AnswerQuizTestOrder>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: AnswerQuizTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            is VariantSelected -> {
                queries.setSelectedVariant(event.variantNumber, id)
                if (queries.isSelectedVariantCorrect(id).executeAsOne().asBoolean()) {
                    onCorrectAnswer(id)
                } else {
                    onWrongAnswer(id)
                    QuizComposer.composeWhereItNeeds()
                    issueOrder(Vibrate)
                }
            }
        }
    }
}