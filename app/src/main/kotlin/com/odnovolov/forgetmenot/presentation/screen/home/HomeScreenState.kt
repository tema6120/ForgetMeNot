package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Deck

class HomeScreenState : FlowableState<HomeScreenState>() {
    var searchText: String by me("")
    var selectedDeckIds: List<Long> by me(emptyList())
    var exportedDeck: Deck? by me<Deck?>(null)
}