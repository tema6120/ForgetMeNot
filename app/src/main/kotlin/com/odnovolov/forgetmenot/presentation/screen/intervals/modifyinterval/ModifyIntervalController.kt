package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.DialogPurpose.ToAddNewInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.DialogPurpose.ToChangeInterval
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalEvent.*
import com.soywiz.klock.DateTimeSpan

class ModifyIntervalController(
    private val intervalsSettings: IntervalsSettings,
    private val modifyIntervalDialogState: ModifyIntervalDialogState,
    private val longTermStateSaver: LongTermStateSaver,
    private val modifyIntervalsScreenStateProvider: ShortTermStateProvider<ModifyIntervalDialogState>
) : BaseController<ModifyIntervalEvent, Nothing>() {
    override fun handle(event: ModifyIntervalEvent) {
        when (event) {
            is IntervalValueChanged -> {
                modifyIntervalDialogState.displayedInterval.value =
                    event.intervalValueText.toIntOrNull()
            }

            is IntervalUnitChanged -> {
                modifyIntervalDialogState.displayedInterval.intervalUnit = event.intervalUnit
            }

            OkButtonClicked -> {
                with(modifyIntervalDialogState) {
                    if (!displayedInterval.isValid()) return
                    val newValue: DateTimeSpan = displayedInterval.toDateTimeSpan()
                    when (dialogPurpose) {
                        ToChangeInterval -> intervalsSettings.modifyInterval(grade, newValue)
                        ToAddNewInterval -> intervalsSettings.addInterval(newValue)
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        modifyIntervalsScreenStateProvider.save(modifyIntervalDialogState)
    }
}