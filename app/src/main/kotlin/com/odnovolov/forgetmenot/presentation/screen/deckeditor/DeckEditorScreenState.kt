package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck

data class DeckEditorScreenState(
    val deck: Deck,
    val initialTab: DeckEditorScreenTab
) {
    enum class DeckEditorScreenTab {
        Settings,
        Content
    }
}