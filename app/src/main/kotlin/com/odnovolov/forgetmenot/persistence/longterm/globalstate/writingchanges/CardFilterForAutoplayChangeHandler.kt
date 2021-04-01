package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.interactor.autoplay.CardFilterForAutoplay
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.dateTimeSpanAdapter
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTimeSpan

class CardFilterForAutoplayChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            CardFilterForAutoplay::areCardsAvailableForExerciseIncluded -> {
                val areCardsAvailableForExerciseIncluded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_CARDS_AVAILABLE_FOR_EXERCISE_INCLUDED,
                    value = areCardsAvailableForExerciseIncluded.toString()
                )
            }
            CardFilterForAutoplay::areAwaitingCardsIncluded -> {
                val areAwaitingCardsIncluded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_AWAITING_CARDS_INCLUDED,
                    value = areAwaitingCardsIncluded.toString()
                )
            }
            CardFilterForAutoplay::areLearnedCardsIncluded -> {
                val areLearnedCardsIncluded = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_LEARNED_CARDS_INCLUDED,
                    value = areLearnedCardsIncluded.toString()
                )
            }
            CardFilterForAutoplay::gradeRange -> {
                val gradeRange = change.newValue as IntRange
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_GRADE_MIN,
                    value = gradeRange.first.toString()
                )
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_GRADE_MAX,
                    value = gradeRange.last.toString()
                )
            }
            CardFilterForAutoplay::lastTestedFromTimeAgo -> {
                val lastTestedFromTimeAgo = change.newValue as DateTimeSpan?
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_LAST_TESTED_FROM_TIME_AGO,
                    value = lastTestedFromTimeAgo?.let(dateTimeSpanAdapter::encode)
                )
            }
            CardFilterForAutoplay::lastTestedToTimeAgo -> {
                val lastAnswerToTimeAgo = change.newValue as DateTimeSpan?
                queries.replace(
                    key = DbKeys.CARD_FILTER_FOR_AUTOPLAY_LAST_TESTED_TO_TIME_AGO,
                    value = lastAnswerToTimeAgo?.let(dateTimeSpanAdapter::encode)
                )
            }
        }
    }
}