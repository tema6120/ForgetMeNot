package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck
import kotlinx.serialization.Serializable

class HomeScreenState : FlowMaker<HomeScreenState>() {
    var searchText: String by flowMaker("")
    var deckSelection: DeckSelection? by flowMaker(null)
    var exportedDeck: Deck? by flowMaker(null)
}

@Serializable
data class DeckSelection(
    val selectedDeckIds: List<Long>,
    val purpose: Purpose
) {
    enum class Purpose {
        General,
        ForExercise,
        ForAutoplay
    }
}