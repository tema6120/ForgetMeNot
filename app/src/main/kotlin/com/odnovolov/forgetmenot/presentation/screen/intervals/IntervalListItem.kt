package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.entity.Interval

sealed class IntervalListItem {
    data class Header(val areIntervalsOn: Boolean) : IntervalListItem()
    data class IntervalWrapper(val interval: Interval) : IntervalListItem()
    data class Footer(val excellentGrade: Int) : IntervalListItem()
}