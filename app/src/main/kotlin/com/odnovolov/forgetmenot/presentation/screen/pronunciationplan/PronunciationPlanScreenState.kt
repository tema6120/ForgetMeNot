package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class PronunciationPlanScreenState(
    tip: Tip?
) : FlowMaker<PronunciationPlanScreenState>() {
    var tip: Tip? by flowMaker(tip)
}