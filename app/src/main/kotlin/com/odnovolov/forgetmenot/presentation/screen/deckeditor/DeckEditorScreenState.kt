package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck

class DeckEditorScreenState(
    relevantDeck: Deck,
    typedDeckName: String = ""
) : FlowMaker<DeckEditorScreenState>() {
    val deck: Deck by flowMaker(relevantDeck)
    var typedDeckName: String by flowMaker(typedDeckName)
}