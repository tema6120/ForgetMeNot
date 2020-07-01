package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.odnovolov.forgetmenot.domain.architecturecomponents.copyableListOf
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.months

class IntervalScheme(
    override val id: Long,
    name: String,
    intervals: CopyableList<Interval>
) : RegistrableFlowableState<IntervalScheme>() {
    var name: String by me(name)
    var intervals: CopyableList<Interval> by me(intervals, CollectionChange::class)

    override fun copy() = IntervalScheme(id, name, intervals.copy())

    companion object {
        val Default by lazy {
            IntervalScheme(
                id = 0,
                name = "",
                intervals = copyableListOf(
                    Interval(id = 0, levelOfKnowledge = 0, value = 8.hours.toDateTimeSpan()),
                    Interval(id = 1, levelOfKnowledge = 1, value = 2.days.toDateTimeSpan()),
                    Interval(id = 2, levelOfKnowledge = 2, value = 7.days.toDateTimeSpan()),
                    Interval(id = 3, levelOfKnowledge = 3, value = 21.days.toDateTimeSpan()),
                    Interval(id = 4, levelOfKnowledge = 4, value = 2.months.toDateTimeSpan()),
                    Interval(id = 5, levelOfKnowledge = 5, value = 6.months.toDateTimeSpan())
                )
            )
        }
    }
}