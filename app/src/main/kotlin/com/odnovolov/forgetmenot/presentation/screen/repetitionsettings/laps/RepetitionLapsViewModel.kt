package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import REPETITION_LAPS_SCOPE_ID
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionLapsViewModel(
    private val dialogState: RepetitionLapsDialogState
) : ViewModel() {
    val isInfinitely: Flow<Boolean> = dialogState.flowOf(RepetitionLapsDialogState::isInfinitely)

    val numberOfLapsInput: String get() = dialogState.numberOfLapsInput

    val numberOfLaps: Flow<Int> = dialogState.flowOf(RepetitionLapsDialogState::numberOfLapsInput)
        .transform { numberOfLapsInput: String ->
            val numberOfLaps: Int? = numberOfLapsInput.toIntOrNull()
            numberOfLaps?.let { emit(it) }
        }

    val isOkButtonEnabled: Flow<Boolean> = dialogState
        .flowOf(RepetitionLapsDialogState::numberOfLapsInput)
        .map { numberOfLapsInput: String ->
            val numberOfLaps: Int? = numberOfLapsInput.toIntOrNull()
            numberOfLaps != null && numberOfLaps > 0
        }

    override fun onCleared() {
        getKoin().getScope(REPETITION_LAPS_SCOPE_ID).close()
    }
}