package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class MotivationalTimerScreenState(
    isTimerEnabled: Boolean,
    timeInput: String
) : FlowMaker<MotivationalTimerScreenState>() {
    var isTimerEnabled: Boolean by flowMaker(isTimerEnabled)
    var timeInput: String by flowMaker(timeInput)
}