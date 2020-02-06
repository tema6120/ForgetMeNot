package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class AddDeckScreenState : FlowableState<AddDeckScreenState>() {
    var typedText: String by me("")
}