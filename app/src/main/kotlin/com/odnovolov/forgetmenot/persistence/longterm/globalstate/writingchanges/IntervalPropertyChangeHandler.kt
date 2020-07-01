package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.soywiz.klock.DateTimeSpan

class IntervalPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.intervalQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        val intervalId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(intervalId).executeAsOne()
        if (!exists) return
        when (change.property) {
            Interval::levelOfKnowledge -> {
                val levelOfKnowledge = change.newValue as Int
                queries.updateLevelOfKnowledge(levelOfKnowledge, intervalId)
            }
            Interval::value -> {
                val value = change.newValue as DateTimeSpan
                queries.updateValue(value, intervalId)
            }
        }
    }
}