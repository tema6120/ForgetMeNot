package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.BaseExerciseController
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual.AnswerManualTestEvent.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardControllerQueries

class AnswerManualTestController(private val id: Long) :
    BaseExerciseController<AnswerManualTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: AnswerManualTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            is HintSelectionChanged -> {
                queries.setHintSelectedRange(event.startIndex, event.endIndex)
            }

            RememberButtonClicked -> {
                onCorrectAnswer(id)
            }

            NotRememberButtonClicked -> {
                onWrongAnswer(id)
            }
        }
    }
}