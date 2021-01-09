package com.odnovolov.forgetmenot.presentation.screen.cardinversion

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class CardInversionScreenState(
    tip: Tip?
) : FlowMaker<CardInversionScreenState>() {
    var tip: Tip? by flowMaker(tip)
}