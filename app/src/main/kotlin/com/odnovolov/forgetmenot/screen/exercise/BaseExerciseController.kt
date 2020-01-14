package com.odnovolov.forgetmenot.screen.exercise

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database

abstract class BaseExerciseController<Event, Order> : BaseController<Event, Order>() {
    private val queries: BaseExerciseControllerQueries = database.baseExerciseControllerQueries

    protected fun onCorrectAnswer(id: Long) {
        with(queries) {
            if (isAnswerCorrect(id).executeAsOne().isAnswerCorrect == true) return
            updateLastAnsweredAt(id)
            incrementLapIfCardIsAnsweredForTheFirstTime(id)
            setIsQuestionDisplayedTrue(id)
            setAnswerCorrect(true, id)
            deleteAllRepeatedCardsOnTheRight(id)
            updateLevelOfKnowledge(id)
        }
    }

    protected fun onWrongAnswer(id: Long) {
        with(queries) {
            if (isAnswerCorrect(id).executeAsOne().isAnswerCorrect == false) return
            updateLastAnsweredAt(id)
            incrementLapIfCardIsAnsweredForTheFirstTime(id)
            setIsQuestionDisplayedTrue(id)
            setAnswerCorrect(false, id)
            addRepeatedCardIfThereIsNotOnTheRight(id)
            updateLevelOfKnowledge(id)
        }
    }
}