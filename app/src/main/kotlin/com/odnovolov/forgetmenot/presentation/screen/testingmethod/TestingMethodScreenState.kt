package com.odnovolov.forgetmenot.presentation.screen.testingmethod

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class TestingMethodScreenState(
    tip: Tip?
) : FlowMaker<TestingMethodScreenState>() {
    var tip: Tip? by flowMaker(tip)
}