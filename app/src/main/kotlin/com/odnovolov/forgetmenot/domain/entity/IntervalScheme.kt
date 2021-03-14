package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.copyableListOf
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.months

class IntervalScheme(
    override val id: Long,
    intervals: CopyableList<Interval>
) : FlowMakerWithRegistry<IntervalScheme>() {
    var intervals: CopyableList<Interval> by flowMakerForCopyableCollection(intervals)

    override fun copy() = IntervalScheme(id, intervals.copy())

    companion object {
        val Default by lazy {
            IntervalScheme(
                id = 0,
                intervals = copyableListOf(
                    Interval(id = 0, grade = 0, value = 8.hours.toDateTimeSpan()),
                    Interval(id = 1, grade = 1, value = 2.days.toDateTimeSpan()),
                    Interval(id = 2, grade = 2, value = 7.days.toDateTimeSpan()),
                    Interval(id = 3, grade = 3, value = 21.days.toDateTimeSpan()),
                    Interval(id = 4, grade = 4, value = 2.months.toDateTimeSpan()),
                    Interval(id = 5, grade = 5, value = 6.months.toDateTimeSpan())
                )
            )
        }
    }
}

fun IntervalScheme.isDefault(): Boolean = this.id == IntervalScheme.Default.id