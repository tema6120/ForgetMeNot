package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit

sealed class LastAnswerFilterEvent {
    object ZeroTimeRadioButtonClicked : LastAnswerFilterEvent()
    object SpecificTimeRadioButtonClicked : LastAnswerFilterEvent()
    class IntervalValueChanged(val intervalValueText: String) : LastAnswerFilterEvent()
    class IntervalUnitChanged(val intervalUnit: IntervalUnit) : LastAnswerFilterEvent()
    object OkButtonClicked : LastAnswerFilterEvent()
}