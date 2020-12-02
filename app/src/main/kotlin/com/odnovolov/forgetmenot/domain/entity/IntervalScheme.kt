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
    name: String,
    intervals: CopyableList<Interval>
) : FlowMakerWithRegistry<IntervalScheme>() {
    var name: String by flowMaker(name)
    var intervals: CopyableList<Interval> by flowMaker(intervals, CollectionChange::class)

    override fun copy() = IntervalScheme(id, name, intervals.copy())

    companion object {
        val Default by lazy {
            IntervalScheme(
                id = 0,
                name = "",
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

fun IntervalScheme.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkIntervalSchemeName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedIntervalSchemes.any { it.name == testingName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}