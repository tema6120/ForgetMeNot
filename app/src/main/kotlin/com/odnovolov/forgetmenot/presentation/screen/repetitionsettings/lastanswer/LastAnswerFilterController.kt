package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterEvent.*
import com.soywiz.klock.DateTimeSpan

class LastAnswerFilterController(
    private val repetitionSettings: RepetitionSettings,
    private val dialogState: LastAnswerFilterDialogState,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<LastAnswerFilterDialogState>
) : BaseController<LastAnswerFilterEvent, Nothing>() {
    override fun handle(event: LastAnswerFilterEvent) {
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
                if (dialogState.isFromDialog) {
                    repetitionSettings.setLastAnswerFromTimeAgo(timeSpan)
                } else {
                    repetitionSettings.setLastAnswerToTimeAgo(timeSpan)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}