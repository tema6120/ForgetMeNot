package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.BaseExerciseController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestEvent.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardControllerQueries

class AnswerOffTestController(private val id: Long) :
    BaseExerciseController<AnswerOffTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: AnswerOffTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            is HintSelectionChanged -> {
                queries.setHintSelectedRange(event.startIndex, event.endIndex)
            }

            ShowAnswerButtonClicked -> {
                onCorrectAnswer(id)
            }
        }
    }
}