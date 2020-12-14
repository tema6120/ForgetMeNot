package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.CardFiltersForAutoplay
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toRepetitionSettingDb
import com.soywiz.klock.DateTimeSpan

class RepetitionSettingPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.repetitionSettingQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val repetitionSettingId: Long = /*change.propertyOwnerId*/ 0L
        val exists: Boolean = queries.exists(repetitionSettingId).executeAsOne()
        if (!exists) {
            // todo: temporary solution
            queries.insert(CardFiltersForAutoplay.Default.toRepetitionSettingDb())
            return
        }
        when (change.property) {
            CardFiltersForAutoplay::isAvailableForExerciseCardsIncluded -> {
                val isAvailableForExerciseCardsIncluded = change.newValue as Boolean
                queries.updateIsAvailableForExerciseCardsIncluded(
                    isAvailableForExerciseCardsIncluded,
                    repetitionSettingId
                )
            }
            CardFiltersForAutoplay::isAwaitingCardsIncluded -> {
                val isAwaitingCardsIncluded = change.newValue as Boolean
                queries.updateIsAwaitingCardsIncluded(isAwaitingCardsIncluded, repetitionSettingId)
            }
            CardFiltersForAutoplay::isLearnedCardsIncluded -> {
                val isLearnedCardsIncluded = change.newValue as Boolean
                queries.updateIsLearnedCardsIncluded(isLearnedCardsIncluded, repetitionSettingId)
            }
            CardFiltersForAutoplay::gradeRange -> {
                val levelOfKnowledgeRange = change.newValue as IntRange
                queries.updateLevelOfKnowledgeRange(
                    levelOfKnowledgeRange.first,
                    levelOfKnowledgeRange.last,
                    repetitionSettingId
                )
            }
            CardFiltersForAutoplay::lastTestedFromTimeAgo -> {
                val lastAnswerFromTimeAgo = change.newValue as DateTimeSpan?
                queries.updateLastAnswerFromTimeAgo(lastAnswerFromTimeAgo, repetitionSettingId)
            }
            CardFiltersForAutoplay::lastTestedToTimeAgo -> {
                val lastAnswerToTimeAgo = change.newValue as DateTimeSpan?
                queries.updateLastAnswerToTimeAgo(lastAnswerToTimeAgo, repetitionSettingId)
            }
        }
    }
}