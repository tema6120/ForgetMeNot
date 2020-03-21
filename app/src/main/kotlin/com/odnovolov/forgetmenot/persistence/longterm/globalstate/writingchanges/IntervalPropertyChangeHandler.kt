package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.persistence.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.soywiz.klock.DateTimeSpan

object IntervalPropertyChangeHandler {
    private val queries = database.intervalQueries

    fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val intervalId = change.propertyOwnerId
        if (!queries.exists(intervalId).executeAsOne()) return
        when (change.property) {
            Interval::targetLevelOfKnowledge -> {
                val targetLevelOfKnowledge = change.newValue as Int
                queries.updateTargetLevelOfKnowledge(targetLevelOfKnowledge, intervalId)
            }
            Interval::value -> {
                val value = change.newValue as DateTimeSpan
                queries.updateValue(value, intervalId)
            }
        }
    }
}