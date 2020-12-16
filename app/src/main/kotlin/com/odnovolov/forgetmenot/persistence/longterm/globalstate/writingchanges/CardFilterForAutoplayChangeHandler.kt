package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.CardFilterForAutoplay
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toRepetitionSettingDb
import com.soywiz.klock.DateTimeSpan

class CardFilterForAutoplayChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.repetitionSettingQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val repetitionSettingId: Long = /*change.propertyOwnerId*/ 0L
        val exists: Boolean = queries.exists(repetitionSettingId).executeAsOne()
        if (!exists) {
            // todo: temporary solution
            queries.insert(CardFilterForAutoplay.Default.toRepetitionSettingDb())
            return
        }
        when (change.property) {
            CardFilterForAutoplay::isAvailableForExerciseCardsIncluded -> {
                val isAvailableForExerciseCardsIncluded = change.newValue as Boolean
                queries.updateIsAvailableForExerciseCardsIncluded(
                    isAvailableForExerciseCardsIncluded,
                    repetitionSettingId
                )
            }
            CardFilterForAutoplay::isAwaitingCardsIncluded -> {
                val isAwaitingCardsIncluded = change.newValue as Boolean
                queries.updateIsAwaitingCardsIncluded(isAwaitingCardsIncluded, repetitionSettingId)
            }
            CardFilterForAutoplay::isLearnedCardsIncluded -> {
                val isLearnedCardsIncluded = change.newValue as Boolean
                queries.updateIsLearnedCardsIncluded(isLearnedCardsIncluded, repetitionSettingId)
            }
            CardFilterForAutoplay::gradeRange -> {
                val levelOfKnowledgeRange = change.newValue as IntRange
                queries.updateLevelOfKnowledgeRange(
                    levelOfKnowledgeRange.first,
                    levelOfKnowledgeRange.last,
                    repetitionSettingId
                )
            }
            CardFilterForAutoplay::lastTestedFromTimeAgo -> {
                val lastAnswerFromTimeAgo = change.newValue as DateTimeSpan?
                queries.updateLastAnswerFromTimeAgo(lastAnswerFromTimeAgo, repetitionSettingId)
            }
            CardFilterForAutoplay::lastTestedToTimeAgo -> {
                val lastAnswerToTimeAgo = change.newValue as DateTimeSpan?
                queries.updateLastAnswerToTimeAgo(lastAnswerToTimeAgo, repetitionSettingId)
            }
        }
    }
}