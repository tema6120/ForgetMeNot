package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class LapsInPlayerDialogState(
    isInfinitely: Boolean,
    numberOfLapsInput: String
) : FlowMaker<LapsInPlayerDialogState>() {
    var isInfinitely: Boolean by flowMaker(isInfinitely)
    var numberOfLapsInput: String by flowMaker(numberOfLapsInput)
}