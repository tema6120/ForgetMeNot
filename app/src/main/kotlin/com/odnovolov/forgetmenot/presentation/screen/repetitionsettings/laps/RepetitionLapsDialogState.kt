package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class RepetitionLapsDialogState(
    isInfinitely: Boolean,
    numberOfLapsInput: String
) : FlowableState<RepetitionLapsDialogState>() {
    var isInfinitely: Boolean by me(isInfinitely)
    var numberOfLapsInput: String by me(numberOfLapsInput)
}