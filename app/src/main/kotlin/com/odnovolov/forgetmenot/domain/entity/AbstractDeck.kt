package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

interface AbstractDeck

class NewDeck(
    deckName: String
) : FlowMaker<NewDeck>(), AbstractDeck {
    var deckName: String by flowMaker(deckName)
}

class ExistingDeck(val deck: Deck) : AbstractDeck

const val ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK = "Unknown implementation of AbstractDeck"