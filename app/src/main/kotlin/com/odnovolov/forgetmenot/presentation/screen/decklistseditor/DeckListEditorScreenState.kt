package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList

class DeckListEditorScreenState(
    isForCreation: Boolean,
    editableDeckListForColorChooser: EditableDeckList? = null
) : FlowMaker<DeckListEditorScreenState>() {
    val isForCreation: Boolean by flowMaker(isForCreation)
    var editableDeckListForColorChooser: EditableDeckList?
            by flowMaker(editableDeckListForColorChooser)
}