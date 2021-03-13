package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.DeckList

class DeckListEditorScreenState(
    isForCreation: Boolean,
    deckListForColorChooser: DeckList? = null
) : FlowMaker<DeckListEditorScreenState>() {
    val isForCreation: Boolean by flowMaker(isForCreation)
    var deckListForColorChooser: DeckList? by flowMaker(deckListForColorChooser)
}