package com.odnovolov.forgetmenot.presentation.screen.intervals

sealed class IntervalsEvent {
    object HelpButtonClicked : IntervalsEvent()
    class ModifyIntervalButtonClicked(val grade: Int) : IntervalsEvent()
    object AddIntervalButtonClicked : IntervalsEvent()
    object RemoveIntervalButtonClicked : IntervalsEvent()
}