package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class DeckSettingsScreenState : FlowableState<DeckSettingsScreenState>() {
    var typedDeckName: String by me("")
}