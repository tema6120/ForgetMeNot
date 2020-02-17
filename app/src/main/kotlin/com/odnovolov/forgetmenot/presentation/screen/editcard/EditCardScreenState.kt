package com.odnovolov.forgetmenot.presentation.screen.editcard

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class EditCardScreenState : FlowableState<EditCardScreenState>() {
    var question: String by me("")
    var answer: String by me("")
}