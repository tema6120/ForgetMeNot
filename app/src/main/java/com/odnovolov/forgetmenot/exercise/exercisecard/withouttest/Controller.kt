package com.odnovolov.forgetmenot.exercise.exercisecard.withouttest

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecard.withouttest.ExerciseCardEvent.ShowAnswerButtonClicked
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries

class ExerciseCardWithoutTestController(private val cardId: Long) :
    BaseController<ExerciseCardEvent, Nothing>() {
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