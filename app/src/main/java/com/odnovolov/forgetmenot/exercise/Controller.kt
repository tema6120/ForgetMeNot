package com.odnovolov.forgetmenot.exercise

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.exercise.ExerciseOrder.MoveToNextPosition

class ExerciseController : BaseController<ExerciseEvent, ExerciseOrder>() {
    private val queries: ExerciseControllerQueries = database.exerciseControllerQueries

    override fun handleEvent(event: ExerciseEvent) {
        return when (event) {
            is NewPageBecameSelected -> {
                val cardIds = queries.getCardIdsInExercise().executeAsList()
                val currentCardId = cardIds[event.position]
                queries.setCurrentCardId(currentCardId)
            }

            NotAskButtonClicked -> {
                queries.setLearnedForCurrentCard(true)
                issueOrder(MoveToNextPosition)
            }

            UndoButtonClicked -> {
                queries.setLearnedForCurrentCard(false)
            }
        }
    }
}