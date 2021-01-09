package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class QuestionDisplayScreenState(
    tip: Tip?
) : FlowMaker<QuestionDisplayScreenState>() {
    var tip: Tip? by flowMaker(tip)
}