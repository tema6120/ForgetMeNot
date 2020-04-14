package com.odnovolov.forgetmenot.persistence.longterm.globalstate.writingchanges

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toIntervalDb

class IntervalSchemePropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    private val queries = database.intervalSchemeQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        val intervalSchemeId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(intervalSchemeId).executeAsOne()
        if (!exists) return
        when (change.property) {
            IntervalScheme::name -> {
                if (change !is PropertyValueChange) return
                val name = change.newValue as String
                queries.updateName(name, intervalSchemeId)
            }
            IntervalScheme::intervals -> {
                if (change !is CollectionChange) return
                val removedIntervals = change.removedItems as Collection<Interval>
                removedIntervals.forEach { interval -> database.intervalQueries.delete(interval.id) }
                val addedIntervals = change.addedItems as Collection<Interval>
                insertIntervals(addedIntervals, intervalSchemeId)
            }
        }
    }

    fun insertIntervals(intervals: Collection<Interval>, intervalSchemeId: Long) {
        intervals.forEach { interval ->
            val intervalDb = interval.toIntervalDb(intervalSchemeId)
            database.intervalQueries.insert(intervalDb)
        }
    }
}