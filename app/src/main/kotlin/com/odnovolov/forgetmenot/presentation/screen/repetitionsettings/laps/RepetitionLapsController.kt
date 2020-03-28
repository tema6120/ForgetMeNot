package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider

class RepetitionLapsController(
    private val repetitionSettings: RepetitionSettings,
    private val dialogState: RepetitionLapsDialogState,
    private val dialogStateProvider: UserSessionTermStateProvider<RepetitionLapsDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) {
    fun onLapsRadioButtonClicked() {
        dialogState.isInfinitely = false
    }

    fun onLapsInputChanged(numberOfLapsInput: String) {
        dialogState.numberOfLapsInput = numberOfLapsInput
    }

    fun onInfinitelyRadioButtonClicked() {
        dialogState.isInfinitely = true
    }

    fun onOkButtonClicked() {
        val numberOfLaps: Int? =
            if (dialogState.isInfinitely) Int.MAX_VALUE
            else dialogState.numberOfLapsInput.toIntOrNull()
        if (numberOfLaps != null && numberOfLaps > 0) {
            repetitionSettings.setNumberOfLaps(numberOfLaps)
            longTermStateSaver.saveStateByRegistry()
        }
    }

    fun onFragmentPause() {
        dialogStateProvider.save(dialogState)
    }
}