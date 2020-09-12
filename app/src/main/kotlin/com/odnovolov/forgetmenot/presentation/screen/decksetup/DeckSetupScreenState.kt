package com.odnovolov.forgetmenot.presentation.screen.decksetup

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck

class DeckSetupScreenState(
    relevantDeck: Deck,
    typedDeckName: String = ""
) : FlowMaker<DeckSetupScreenState>() {
    val relevantDeck: Deck by flowMaker(relevantDeck)
    var typedDeckName: String by flowMaker(typedDeckName)
}