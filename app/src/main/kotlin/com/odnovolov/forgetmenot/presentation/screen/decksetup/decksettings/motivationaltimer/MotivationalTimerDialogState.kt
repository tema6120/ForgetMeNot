package com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.motivationaltimer

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class MotivationalTimerDialogState(
    isTimerEnabled: Boolean,
    timeInput: String
) : FlowableState<MotivationalTimerDialogState>() {
    var isTimerEnabled: Boolean by me(isTimerEnabled)
    var timeInput: String by me(timeInput)
}