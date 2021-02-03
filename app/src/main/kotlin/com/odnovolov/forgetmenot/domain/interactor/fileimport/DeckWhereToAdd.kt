package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck

interface DeckWhereToAdd

class NewDeck(
    deckName: String
) : FlowMaker<NewDeck>(), DeckWhereToAdd {
    var deckName: String by flowMaker(deckName)
}

class ExistingDeck(val deck: Deck) : DeckWhereToAdd