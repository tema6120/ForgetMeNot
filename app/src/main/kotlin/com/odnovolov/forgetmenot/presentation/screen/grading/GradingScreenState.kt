package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip

class GradingScreenState(
    tip: Tip?,
    dialogPurpose: DialogPurpose? = null
) : FlowMaker<GradingScreenState>() {
    var tip: Tip? by flowMaker(tip)
    var dialogPurpose: DialogPurpose? by flowMaker(dialogPurpose)

    enum class DialogPurpose{
        ToChangeGradingOnFirstCorrectAnswer,
        ToChangeGradingOnFirstWrongAnswer,
        ToChangeGradingOnRepeatedCorrectAnswer,
        ToChangeGradingOnRepeatedWrongAnswer
    }
}