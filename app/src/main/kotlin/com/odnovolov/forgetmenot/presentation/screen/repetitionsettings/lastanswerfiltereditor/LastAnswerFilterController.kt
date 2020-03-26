package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswerfiltereditor

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval.IntervalUnit
import com.soywiz.klock.DateTimeSpan

class LastAnswerFilterController(
    private val repetitionSettings: RepetitionSettings,
    private val dialogState: LastAnswerFilterDialogState,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: UserSessionTermStateProvider<LastAnswerFilterDialogState>
) {
    fun onZeroTimeRadioButtonClicked() {
        dialogState.isZeroTimeSelected = true
    }

    fun onSpecificTimeRadioButtonClicked() {
        dialogState.isZeroTimeSelected = false
    }

    fun onIntervalValueChanged(intervalValueText: String) {
        dialogState.timeAgo.value = intervalValueText.toIntOrNull()
    }

    fun onIntervalUnitChanged(intervalUnit: IntervalUnit) {
        dialogState.timeAgo.intervalUnit = intervalUnit
    }

    fun onOkButtonClicked() {
        val timeSpan: DateTimeSpan? =
            if (dialogState.isZeroTimeSelected) {
                null
            } else {
                if (dialogState.timeAgo.isValid()) {
                    dialogState.timeAgo.toDateTimeSpan()
                } else {
                    return
                }
            }
        if (dialogState.isFromDialog) {
            repetitionSettings.setLastAnswerFromTimeAgo(timeSpan)
        } else {
            repetitionSettings.setLastAnswerToTimeAgo(timeSpan)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onFragmentPause() {
        dialogStateProvider.save(dialogState)
    }
}