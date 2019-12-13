package com.odnovolov.forgetmenot.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.screen.intervals.IntervalUnit

sealed class ModifyIntervalEvent {
    class IntervalNumberChanged(val intervalNumberText: CharSequence?) : ModifyIntervalEvent()
    class IntervalUnitChanged(val intervalUnit: IntervalUnit) : ModifyIntervalEvent()
    object OkButtonClicked : ModifyIntervalEvent()
}