package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class CardsThresholdDialogState(
    text: String,
    purpose: Purpose? = null
) : FlowMaker<CardsThresholdDialogState>() {
    var text: String by flowMaker(text)
    var purpose: Purpose? by flowMaker(purpose)

    enum class Purpose {
        ToChangeCardNumberLimitation,
        ToChangeCardNumberThresholdForShowingFilter
    }
}