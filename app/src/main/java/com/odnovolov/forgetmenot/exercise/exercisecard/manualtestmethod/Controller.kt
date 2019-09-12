package com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodEvent.NotRememberButtonClicked
import com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodEvent.RememberButtonClicked
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries

class ExerciseCardManualTestMethodController(private val cardId: Long)
    : BaseController<ExerciseCardManualTestMethodEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardManualTestMethodEvent) {
        when (event) {
            RememberButtonClicked -> {
                queries.incrementLapIfNeed(cardId)
                queries.setAnswerCorrect(true, cardId)
            }
            NotRememberButtonClicked -> {
                queries.incrementLapIfNeed(cardId)
                queries.setAnswerCorrect(false, cardId)
            }
        }
    }
}