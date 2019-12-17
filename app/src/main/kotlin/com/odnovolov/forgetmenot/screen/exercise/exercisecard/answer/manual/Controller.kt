package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardControllerQueries
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual.AnswerManualTestEvent.*

class AnswerManualTestController(private val id: Long) :
    BaseController<AnswerManualTestEvent, Nothing>() {
    private val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    override fun handleEvent(event: AnswerManualTestEvent) {
        when (event) {
            is AnswerTextSelectionChanged -> {
                queries.setAnswerTextSelection(event.selection)
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