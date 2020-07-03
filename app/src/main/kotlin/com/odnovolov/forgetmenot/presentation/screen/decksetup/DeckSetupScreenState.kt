package com.odnovolov.forgetmenot.presentation.screen.decksetup

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Deck

class DeckSetupScreenState(
    relevantDeck: Deck,
    typedDeckName: String = ""
) : FlowableState<DeckSetupScreenState>() {
    val relevantDeck: Deck by me(relevantDeck)
    var typedDeckName: String by me(typedDeckName)
}