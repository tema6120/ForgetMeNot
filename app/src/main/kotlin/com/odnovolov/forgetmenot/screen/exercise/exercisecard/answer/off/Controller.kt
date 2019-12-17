package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestEvent.*

class AnswerOffTestController(private val id: Long) :
    BaseController<AnswerOffTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: AnswerOffTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            ShowAnswerButtonClicked -> {
                queries.updateLastAnsweredAt(id)
                queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
                queries.setIsQuestionDisplayedTrue(id)
                queries.setAnswerCorrect(true, id)
            }
        }
    }
}