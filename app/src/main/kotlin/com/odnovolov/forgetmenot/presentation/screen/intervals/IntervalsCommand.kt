package com.odnovolov.forgetmenot.presentation.screen.intervals

sealed class IntervalsCommand {
    object ShowModifyIntervalDialog : IntervalsCommand()
    class SetNamePresetDialogText(val text: String) : IntervalsCommand()
}