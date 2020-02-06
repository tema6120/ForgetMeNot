package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.Copyable
import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.copyableListOf
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.months

class IntervalScheme(
    override val id: Long = 0,
    name: String = "",
    intervals: CopyableList<Interval> = copyableListOf(
        Interval(id = 1, targetLevelOfKnowledge = 1, value = 8.hours.toDateTimeSpan()),
        Interval(id = 2, targetLevelOfKnowledge = 2, value = 2.days.toDateTimeSpan()),
        Interval(id = 3, targetLevelOfKnowledge = 3, value = 7.days.toDateTimeSpan()),
        Interval(id = 4, targetLevelOfKnowledge = 4, value = 21.days.toDateTimeSpan()),
        Interval(id = 5, targetLevelOfKnowledge = 5, value = 2.months.toDateTimeSpan()),
        Interval(id = 6, targetLevelOfKnowledge = 6, value = 6.months.toDateTimeSpan())
    )
) : RegistrableFlowableState<IntervalScheme>(), Copyable {
    var name: String by me(name)
    val intervals: CopyableList<Interval> by me(intervals)

    override fun copy() = IntervalScheme(id, name, intervals.copy())

    companion object {
        val Default = IntervalScheme()
    }
}