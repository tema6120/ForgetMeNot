package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardEvent.QuestionTextSelectionChanged
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardEvent.ShowQuestionButtonClicked
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardControllerQueries

class ExerciseCardController(private val id: Long) : BaseController<ExerciseCardEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardEvent) {
        return when (event) {
            ShowQuestionButtonClicked -> {
                queries.setIsQuestionDisplayedTrue(id)
            }

            is QuestionTextSelectionChanged -> {
                queries.setQuestionTextSelection(event.selection)
            }
        }
    }
}