package com.odnovolov.forgetmenot.presentation.screen.decksetup

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class DeckSetupScreenState : FlowableState<DeckSetupScreenState>() {
    var typedDeckName: String by me("")
}