package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
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
    var intervals: CopyableList<Interval> by me(intervals)

    override fun copy() = IntervalScheme(id, name, intervals.copy())

    companion object {
        val Default by lazy {
            IntervalScheme(
                id = 0L,
                name = "",
                intervals = copyableListOf(
                    Interval(id = 1L, targetLevelOfKnowledge = 1, value = 8.hours.toDateTimeSpan()),
                    Interval(id = 2L, targetLevelOfKnowledge = 2, value = 2.days.toDateTimeSpan()),
                    Interval(id = 3L, targetLevelOfKnowledge = 3, value = 7.days.toDateTimeSpan()),
                    Interval(id = 4L, targetLevelOfKnowledge = 4, value = 21.days.toDateTimeSpan()),
                    Interval(id = 5L, targetLevelOfKnowledge = 5, value = 2.months.toDateTimeSpan()),
                    Interval(id = 6L, targetLevelOfKnowledge = 6, value = 6.months.toDateTimeSpan())
                )
            )
        }
    }
}