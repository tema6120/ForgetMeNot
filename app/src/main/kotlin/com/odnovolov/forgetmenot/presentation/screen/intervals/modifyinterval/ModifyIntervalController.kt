package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit

class ModifyIntervalController(
    private val intervalsSettings: IntervalsSettings,
    private val modifyIntervalDialogState: ModifyIntervalDialogState,
    private val longTermStateSaver: LongTermStateSaver,
    private val modifyIntervalsScreenStateProvider: UserSessionTermStateProvider<ModifyIntervalDialogState>
) {
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
                longTermStateSaver.saveStateByRegistry()
            }
        }
    }

    fun onFragmentPause() {
        modifyIntervalsScreenStateProvider.save(modifyIntervalDialogState)
    }
}