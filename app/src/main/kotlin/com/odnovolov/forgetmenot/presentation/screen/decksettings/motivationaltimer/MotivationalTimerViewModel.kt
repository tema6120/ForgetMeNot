package com.odnovolov.forgetmenot.presentation.screen.decksettings.motivationaltimer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class MotivationalTimerViewModel(
    private val dialogState: MotivationalTimerDialogState
) {
    val isTimerEnabled: Flow<Boolean> =
        dialogState.flowOf(MotivationalTimerDialogState::isTimerEnabled)

    val timeInput: String get() = dialogState.timeInput

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isTimerEnabled,
        dialogState.flowOf(MotivationalTimerDialogState::timeInput)
    ) { isTimerEnabled: Boolean, timeInput: String ->
        if (!isTimerEnabled) {
            true
        } else {
            val timeForAnswer: Int? = timeInput.toIntOrNull()
            timeForAnswer != null && timeForAnswer > 0
        }
    }
}