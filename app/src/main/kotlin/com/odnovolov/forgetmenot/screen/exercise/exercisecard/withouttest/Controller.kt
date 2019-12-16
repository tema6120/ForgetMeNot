package com.odnovolov.forgetmenot.screen.exercise.exercisecard.withouttest

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.withouttest.ExerciseCardWithoutTestEvent.*

class ExerciseCardWithoutTestController(private val id: Long) :
    BaseController<ExerciseCardWithoutTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardWithoutTestEvent) {
        when (event) {
            is QuestionTextSelectionChanged -> {
                queries.setQuestionTextSelection(event.selection)
            }

            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
            }

            ShowQuestionButtonClicked -> {
                queries.setIsQuestionDisplayedTrue(id)
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