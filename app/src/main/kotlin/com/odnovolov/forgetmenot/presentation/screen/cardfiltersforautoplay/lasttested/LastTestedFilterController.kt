package com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.lasttested

import com.odnovolov.forgetmenot.domain.entity.CardFiltersForAutoplay
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardfiltersforautoplay.lasttested.LastTestedFilterEvent.*
import com.soywiz.klock.DateTimeSpan

class LastTestedFilterController(
    private val cardFilters: CardFiltersForAutoplay,
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
                    cardFilters.lastTestedFromTimeAgo = timeSpan else
                    cardFilters.lastTestedToTimeAgo = timeSpan
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}