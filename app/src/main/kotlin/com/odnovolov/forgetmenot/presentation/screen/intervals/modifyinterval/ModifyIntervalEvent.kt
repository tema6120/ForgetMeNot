package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit

sealed class ModifyIntervalEvent {
    class IntervalValueChanged(val intervalValueText: String) : ModifyIntervalEvent()
    class IntervalUnitChanged(val intervalUnit: IntervalUnit) : ModifyIntervalEvent()
    object OkButtonClicked : ModifyIntervalEvent()
}