package com.odnovolov.forgetmenot.presentation.screen.lasttested

import com.odnovolov.forgetmenot.domain.entity.CardFilterLastTested
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.lasttested.LastTestedFilterEvent.*
import com.soywiz.klock.DateTimeSpan

class LastTestedFilterController(
    private val cardFilter: CardFilterLastTested,
    private val dialogState: LastTestedFilterDialogState,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<LastTestedFilterDialogState>
) : BaseController<LastTestedFilterEvent, Nothing>() {
    override fun handle(event: LastTestedFilterEvent) {
        when (event) {
            ZeroTimeRadioButtonClicked -> {
                dialogState.isZeroTimeSelected = true
            }

            SpecificTimeRadioButtonClicked -> {
                dialogState.isZeroTimeSelected = false
            }

            is IntervalValueChanged -> {
                dialogState.timeAgo.value = event.intervalValueText.toIntOrNull()
            }

            is IntervalUnitChanged -> {
                dialogState.timeAgo.intervalUnit = event.intervalUnit
            }

            OkButtonClicked -> {
                val timeSpan: DateTimeSpan? = when {
                    dialogState.isZeroTimeSelected -> null
                    dialogState.timeAgo.isValid() -> dialogState.timeAgo.toDateTimeSpan()
                    else -> return
                }
                if (dialogState.isFromDialog)
                    cardFilter.lastTestedFromTimeAgo = timeSpan else
                    cardFilter.lastTestedToTimeAgo = timeSpan
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}