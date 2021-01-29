package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform

class LapsInPlayerViewModel(
    private val dialogState: LapsInPlayerDialogState
) {
    val isInfinitely: Flow<Boolean> = dialogState.flowOf(LapsInPlayerDialogState::isInfinitely)

    val numberOfLapsInput: String get() = dialogState.numberOfLapsInput

    val numberOfLaps: Flow<Int> = dialogState.flowOf(LapsInPlayerDialogState::numberOfLapsInput)
        .transform { numberOfLapsInput: String ->
            val numberOfLaps: Int? = numberOfLapsInput.toIntOrNull()
            numberOfLaps?.let { emit(it) }
        }

    val isOkButtonEnabled: Flow<Boolean> = combine(
        isInfinitely,
        dialogState.flowOf(LapsInPlayerDialogState::numberOfLapsInput)
    ) { isInfinitely: Boolean, numberOfLapsInput: String ->
        if (isInfinitely) {
            true
        } else {
            val numberOfLaps: Int? = numberOfLapsInput.toIntOrNull()
            numberOfLaps != null && numberOfLaps > 0
        }
    }
}