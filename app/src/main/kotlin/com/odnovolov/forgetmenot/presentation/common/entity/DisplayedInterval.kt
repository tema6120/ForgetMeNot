package com.odnovolov.forgetmenot.presentation.common.entity

import android.content.Context
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit.*
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit.Months
import com.soywiz.klock.*

class DisplayedInterval(
    value: Int?,
    intervalUnit: IntervalUnit
): FlowMaker<DisplayedInterval>() {
    var value: Int? by flowMaker(value)
    var intervalUnit: IntervalUnit by flowMaker(intervalUnit)

    fun isValid(): Boolean = value.let { value -> value != null && value > 0 }

    fun toDateTimeSpan(): DateTimeSpan {
        if (!isValid()) throw IllegalStateException("intervalNumber is not valid")
        return when (intervalUnit) {
            Hours -> value!!.hours.toDateTimeSpan()
            Days -> value!!.days.toDateTimeSpan()
            Months -> value!!.months.toDateTimeSpan()
        }
    }

    fun toString(context: Context): String {
        val intervalUnit: String = context.getString(
            when (intervalUnit) {
                Hours -> R.string.interval_unit_hours
                Days -> R.string.interval_unit_days
                Months -> R.string.interval_unit_months
            }
        )
        return "$value $intervalUnit"
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