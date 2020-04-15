package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform

class RepetitionLapsViewModel(
    private val dialogState: RepetitionLapsDialogState
) {
    val isInfinitely: Flow<Boolean> = dialogState.flowOf(RepetitionLapsDialogState::isInfinitely)

    val numberOfLapsInput: String get() = dialogState.numberOfLapsInput

    val numberOfLaps: Flow<Int> = dialogState.flowOf(RepetitionLapsDialogState::numberOfLapsInput)
        .transform { numberOfLapsInput: String ->
            val numberOfLaps: Int? = numberOfLapsInput.toIntOrNull()
            numberOfLaps?.let { emit(it) }
        }

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isInfinitely,
        dialogState.flowOf(RepetitionLapsDialogState::numberOfLapsInput)
    ) { isInfinitely: Boolean, numberOfLapsInput: String ->
        if (isInfinitely) {
            true
        } else {
            val numberOfLaps: Int? = numberOfLapsInput.toIntOrNull()
            numberOfLaps != null && numberOfLaps > 0
        }
    }
}