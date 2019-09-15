package com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodEvent.NotRememberButtonClicked
import com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodEvent.RememberButtonClicked
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries

class ExerciseCardManualTestMethodController(private val id: Long) :
    BaseController<ExerciseCardManualTestMethodEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardManualTestMethodEvent) {
        when (event) {
            RememberButtonClicked -> {
                if (queries.isAnswerCorrect(id).executeAsOne().isAnswerCorrect == true) return
                queries.updateLastAnsweredAt(id)
                queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
                queries.setAnswerCorrect(true, id)
                queries.deleteRepeatedCards(id)
            }

            NotRememberButtonClicked -> {
                if (queries.isAnswerCorrect(id).executeAsOne().isAnswerCorrect == false) return
                queries.updateLastAnsweredAt(id)
                queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
                queries.setAnswerCorrect(false, id)
                if (queries.isThereRepeatedCard(id).executeAsOne().asBoolean().not()) {
                    queries.addRepeatedCard(id)
                }
            }
        }
    }
}