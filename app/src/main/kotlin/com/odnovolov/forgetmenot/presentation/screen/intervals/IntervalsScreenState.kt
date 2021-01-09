package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class IntervalsScreenState(
    tip: Tip?
) : FlowMaker<IntervalsScreenState>() {
    var tip: Tip? by flowMaker(tip)
}