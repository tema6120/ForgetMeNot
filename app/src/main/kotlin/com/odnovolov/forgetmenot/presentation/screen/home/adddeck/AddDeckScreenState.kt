package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class AddDeckScreenState : FlowableState<AddDeckScreenState>() {
    var typedText: String by me("")
    var howToAdd: HowToAdd? by me<HowToAdd?>(null)

    enum class HowToAdd {
        LOAD_FROM_FILE,
        CREATE
    }
}