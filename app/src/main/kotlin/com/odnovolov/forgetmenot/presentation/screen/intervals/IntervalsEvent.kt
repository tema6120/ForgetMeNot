package com.odnovolov.forgetmenot.presentation.screen.intervals

sealed class IntervalsEvent {
    class ModifyIntervalButtonClicked(val targetLevelOfKnowledge: Int) : IntervalsEvent()
    object AddIntervalButtonClicked : IntervalsEvent()
    object RemoveIntervalButtonClicked : IntervalsEvent()
}