package com.odnovolov.forgetmenot.exercise.exercisecards

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardEvent.ShowAnswerButtonClicked

class ExerciseCardController(private val cardId: Long)
    : BaseController<ExerciseCardEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardEvent) {
        return when (event) {
            ShowAnswerButtonClicked -> {
                queries.setAnswered(cardId)
                queries.incrementLap(cardId)
            }
        }
    }
}