package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import com.odnovolov.forgetmenot.domain.entity.DeckList

data class DeckListEditorScreenState(
    val isForCreation: Boolean,
    var deckListForColorChooser: DeckList? = null
)