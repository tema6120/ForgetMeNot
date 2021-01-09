package com.odnovolov.forgetmenot.presentation.screen.intervals

sealed class IntervalsEvent {
    object HelpButtonClicked : IntervalsEvent()
    object CloseTipButtonClicked : IntervalsEvent()
    object IntervalsSwitchToggled : IntervalsEvent()
    class IntervalButtonClicked(val grade: Int) : IntervalsEvent()
    object AddIntervalButtonClicked : IntervalsEvent()
    object RemoveIntervalButtonClicked : IntervalsEvent()
}