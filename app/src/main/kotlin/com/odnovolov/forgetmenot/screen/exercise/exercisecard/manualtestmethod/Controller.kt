package com.odnovolov.forgetmenot.screen.exercise.exercisecard.manualtestmethod

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodEvent.*

class ExerciseCardManualTestMethodController(private val id: Long) :
    BaseController<ExerciseCardManualTestMethodEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: ExerciseCardManualTestMethodEvent) {
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

            RememberButtonClicked -> {
                if (queries.isAnswerCorrect(id).executeAsOne().isAnswerCorrect == true) return
                queries.updateLastAnsweredAt(id)
                queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
                queries.setIsQuestionDisplayedTrue(id)
                queries.setAnswerCorrect(true, id)
                queries.deleteAllRepeatedCardsOnTheRight(id)
                queries.updateLevelOfKnowledge(id)
            }

            NotRememberButtonClicked -> {
                if (queries.isAnswerCorrect(id).executeAsOne().isAnswerCorrect == false) return
                queries.updateLastAnsweredAt(id)
                queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
                queries.setIsQuestionDisplayedTrue(id)
                queries.setAnswerCorrect(false, id)
                if (queries.isThereAnyRepeatedCardOnTheRight(id).executeAsOne().asBoolean().not()) {
                    queries.addRepeatedCard(id)
                }
                queries.updateLevelOfKnowledge(id)
            }
        }
    }
}