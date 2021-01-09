package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class MotivationalTimerScreenState(
    tip: Tip?,
    isTimerEnabled: Boolean,
    timeInput: String
) : FlowMaker<MotivationalTimerScreenState>() {
    var tip: Tip? by flowMaker(tip)
    var isTimerEnabled: Boolean by flowMaker(isTimerEnabled)
    var timeInput: String by flowMaker(timeInput)
}