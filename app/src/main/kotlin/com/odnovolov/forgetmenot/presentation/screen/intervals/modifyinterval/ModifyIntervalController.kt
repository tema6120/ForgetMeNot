package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit

class ModifyIntervalController(
    private val modifyIntervalDialogState: ModifyIntervalDialogState,
    private val store: Store
) {
    private var isFragmentRemoving = false

    fun onIntervalValueChanged(intervalValueText: String) {
        modifyIntervalDialogState.displayedInterval.value = intervalValueText.toIntOrNull()
    }

    fun onIntervalUnitChanged(intervalUnit: IntervalUnit) {
        modifyIntervalDialogState.displayedInterval.intervalUnit = intervalUnit
    }

    fun onOkButtonClicked() {
        with(modifyIntervalDialogState) {
            if (displayedInterval.isValid()) {
                interval.value = displayedInterval.toDateTimeSpan()
                store.saveStateByRegistry()
            }
        }
    }

    fun onFragmentRemoving() {
        isFragmentRemoving = true
    }

    fun onCleared() {
        if (isFragmentRemoving) {
            store.deleteModifyIntervalDialogState()
        } else {
            store.save(modifyIntervalDialogState)
        }
    }
}