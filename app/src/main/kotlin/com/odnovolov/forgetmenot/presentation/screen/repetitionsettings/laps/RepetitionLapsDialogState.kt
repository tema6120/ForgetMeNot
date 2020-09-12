package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class RepetitionLapsDialogState(
    isInfinitely: Boolean,
    numberOfLapsInput: String
) : FlowMaker<RepetitionLapsDialogState>() {
    var isInfinitely: Boolean by flowMaker(isInfinitely)
    var numberOfLapsInput: String by flowMaker(numberOfLapsInput)
}