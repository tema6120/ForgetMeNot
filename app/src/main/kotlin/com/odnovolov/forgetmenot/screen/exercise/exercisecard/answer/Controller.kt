package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.exercise.exercisecards.ExerciseCardControllerQueries

abstract class AnswerController<Event, Order>(protected val id: Long) :
    BaseController<Event, Order>() {
    protected val queries: ExerciseCardControllerQueries = database.exerciseCardControllerQueries

    protected fun onCorrectAnswer() {
        if (queries.isAnswerCorrect(id).executeAsOne().isAnswerCorrect == true) return
        queries.updateLastAnsweredAt(id)
        queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
        queries.setIsQuestionDisplayedTrue(id)
        queries.setAnswerCorrect(true, id)
        queries.deleteAllRepeatedCardsOnTheRight(id)
        queries.updateLevelOfKnowledge(id)
    }

    protected fun onWrongAnswer() {
        if (queries.isAnswerCorrect(id).executeAsOne().isAnswerCorrect == false) return
        queries.updateLastAnsweredAt(id)
        queries.incrementLapIfCardIsAnsweredForTheFirstTime(id)
        queries.setIsQuestionDisplayedTrue(id)
        queries.setAnswerCorrect(false, id)
        queries.addRepeatedCardIfThereIsNotOnTheRight(id)
        queries.updateLevelOfKnowledge(id)
    }
}