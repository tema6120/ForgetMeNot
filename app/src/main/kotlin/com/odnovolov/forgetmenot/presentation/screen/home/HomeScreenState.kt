package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck

class HomeScreenState : FlowMaker<HomeScreenState>() {
    var searchText: String by flowMaker("")
    var selectedDeckIds: List<Long> by flowMaker(emptyList())
    var exportedDeck: Deck? by flowMaker(null)
}