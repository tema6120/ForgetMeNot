package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsEvent.*

class RepetitionLapsController(
    private val repetitionSettings: RepetitionSettings,
    private val dialogState: RepetitionLapsDialogState,
    private val dialogStateProvider: ShortTermStateProvider<RepetitionLapsDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<RepetitionLapsEvent, Nothing>() {
    override fun handle(event: RepetitionLapsEvent) {
        when (event) {
            LapsRadioButtonClicked -> {
                dialogState.isInfinitely = false
            }

            is LapsInputChanged -> {
                dialogState.numberOfLapsInput = event.numberOfLapsInput
            }

            InfinitelyRadioButtonClicked -> {
                dialogState.isInfinitely = true
            }

            OkButtonClicked -> {
                val numberOfLaps: Int? =
                    if (dialogState.isInfinitely) Int.MAX_VALUE
                    else dialogState.numberOfLapsInput.toIntOrNull()
                if (numberOfLaps != null && numberOfLaps > 0) {
                    repetitionSettings.setNumberOfLaps(numberOfLaps)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}