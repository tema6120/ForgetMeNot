package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit

sealed class ModifyIntervalEvent {
    class IntervalValueChanged(val intervalValueText: String) : ModifyIntervalEvent()
    class IntervalUnitChanged(val intervalUnit: IntervalUnit) : ModifyIntervalEvent()
    object OkButtonClicked : ModifyIntervalEvent()
}