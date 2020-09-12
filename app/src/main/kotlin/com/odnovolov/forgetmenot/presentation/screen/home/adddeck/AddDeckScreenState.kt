package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class AddDeckScreenState : FlowMaker<AddDeckScreenState>() {
    var typedText: String by flowMaker("")
    var howToAdd: HowToAdd? by flowMaker<HowToAdd?>(null)

    enum class HowToAdd {
        LOAD_FROM_FILE,
        CREATE
    }
}