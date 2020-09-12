package com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.motivationaltimer

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class MotivationalTimerDialogState(
    isTimerEnabled: Boolean,
    timeInput: String
) : FlowMaker<MotivationalTimerDialogState>() {
    var isTimerEnabled: Boolean by flowMaker(isTimerEnabled)
    var timeInput: String by flowMaker(timeInput)
}