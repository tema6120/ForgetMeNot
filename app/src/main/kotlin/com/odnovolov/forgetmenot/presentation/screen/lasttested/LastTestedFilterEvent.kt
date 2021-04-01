package com.odnovolov.forgetmenot.presentation.screen.lasttested

import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit

sealed class LastTestedFilterEvent {
    object ZeroTimeRadioButtonClicked : LastTestedFilterEvent()
    object SpecificTimeRadioButtonClicked : LastTestedFilterEvent()
    class IntervalValueChanged(val intervalValueText: String) : LastTestedFilterEvent()
    class IntervalUnitChanged(val intervalUnit: IntervalUnit) : LastTestedFilterEvent()
    object OkButtonClicked : LastTestedFilterEvent()
}