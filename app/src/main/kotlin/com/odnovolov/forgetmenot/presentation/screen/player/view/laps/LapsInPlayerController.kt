package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.player.view.laps.LapsInPlayerEvent.*

class LapsInPlayerController(
    private val player: Player,
    private val dialogState: LapsInPlayerDialogState,
    private val dialogStateProvider: ShortTermStateProvider<LapsInPlayerDialogState>,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<LapsInPlayerEvent, Nothing>() {
    override fun handle(event: LapsInPlayerEvent) {
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
                    player.setNumberOfLaps(numberOfLaps)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}