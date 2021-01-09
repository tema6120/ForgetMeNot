package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class PronunciationScreenState(
    tip: Tip?
) : FlowMaker<PronunciationScreenState>() {
    var tip: Tip? by flowMaker(tip)
}