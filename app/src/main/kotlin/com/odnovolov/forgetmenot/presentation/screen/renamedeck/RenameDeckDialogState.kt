package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

class RenameDeckDialogState(
    abstractDeck: AbstractDeck,
    typedDeckName: String
) : FlowMaker<RenameDeckDialogState>() {
    val abstractDeck: AbstractDeck by flowMaker(abstractDeck)
    var typedDeckName: String by flowMaker(typedDeckName)
}