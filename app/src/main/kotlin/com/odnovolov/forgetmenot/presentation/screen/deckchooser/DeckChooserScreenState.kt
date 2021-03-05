package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class DeckChooserScreenState(
    purpose: Purpose
) : FlowMaker<DeckChooserScreenState>() {
    val purpose: Purpose by flowMaker(purpose)
    var searchText: String by flowMaker("")

    enum class Purpose {
        ToImportCards,
        ToMergeInto,
        ToMoveCard,
        ToCopyCard,
        ToMoveCardsInDeckEditor,
        ToCopyCardsInDeckEditor,
        ToMoveCardsInSearch,
        ToCopyCardsInSearch
    }
}