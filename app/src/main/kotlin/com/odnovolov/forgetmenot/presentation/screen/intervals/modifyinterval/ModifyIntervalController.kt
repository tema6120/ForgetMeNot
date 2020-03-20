package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.StateProvider
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.intervals.DisplayedInterval.IntervalUnit

class ModifyIntervalController(
    private val intervalsSettings: IntervalsSettings,
    private val modifyIntervalDialogState: ModifyIntervalDialogState,
    private val store: Store,
    private val modifyIntervalsScreenStateProvider: StateProvider<ModifyIntervalDialogState>
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
                intervalsSettings.modifyInterval(
                    targetLevelOfKnowledge = targetLevelOfKnowledge,
                    newValue = displayedInterval.toDateTimeSpan()
                )
                store.saveStateByRegistry()
            }
        }
    }

    fun onFragmentRemoving() {
        isFragmentRemoving = true
    }

    fun onCleared() {
        if (isFragmentRemoving) {
            modifyIntervalsScreenStateProvider.delete()
        } else {
            modifyIntervalsScreenStateProvider.save(modifyIntervalDialogState)
        }
    }
}