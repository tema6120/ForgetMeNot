package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit.*
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit.Months
import com.soywiz.klock.*

class DisplayedInterval(
    value: Int?,
    intervalUnit: IntervalUnit
): FlowableState<DisplayedInterval>() {
    var value: Int? by me(value)
    var intervalUnit: IntervalUnit by me(intervalUnit)

    fun isValid(): Boolean = value?.let { it > 0 } ?: false

    fun toDateTimeSpan(): DateTimeSpan {
        if (!isValid()) throw IllegalStateException("intervalNumber is not valid")
        return when (intervalUnit) {
            Hours -> value!!.hours.toDateTimeSpan()
            Days -> value!!.days.toDateTimeSpan()
            Months -> value!!.months.toDateTimeSpan()
        }
    }

    companion object {
        fun fromDateTimeSpan(dateTimeSpan: DateTimeSpan): DisplayedInterval {
            return when {
                dateTimeSpan.monthSpan.totalMonths != 0 -> {
                    DisplayedInterval(
                        value = dateTimeSpan.monthSpan.totalMonths,
                        intervalUnit = Months
                    )
                }
                dateTimeSpan.timeSpan % 1.days == TimeSpan.ZERO -> {
                    DisplayedInterval(
                        value = dateTimeSpan.timeSpan.days.toInt(),
                        intervalUnit = Days
                    )
                }
                else -> {
                    DisplayedInterval(
                        value = dateTimeSpan.timeSpan.hours.toInt(),
                        intervalUnit = Hours
                    )
                }
            }
        }
    }

    enum class IntervalUnit {
        Hours,
        Days,
        Months
    }
}