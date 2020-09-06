package com.odnovolov.forgetmenot.presentation.screen.intervals

sealed class IntervalsEvent {
    object HelpButtonClicked : IntervalsEvent()
    class ModifyIntervalButtonClicked(val levelOfKnowledge: Int) : IntervalsEvent()
    object AddIntervalButtonClicked : IntervalsEvent()
    object RemoveIntervalButtonClicked : IntervalsEvent()
}