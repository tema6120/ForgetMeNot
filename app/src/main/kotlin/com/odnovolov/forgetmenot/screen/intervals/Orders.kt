package com.odnovolov.forgetmenot.screen.intervals

sealed class IntervalsOrder {
    object ShowModifyIntervalDialog : IntervalsOrder()
    class SetDialogStatus(val text: String) : IntervalsOrder()
}