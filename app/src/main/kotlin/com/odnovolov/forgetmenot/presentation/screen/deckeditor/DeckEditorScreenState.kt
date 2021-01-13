package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck

class DeckEditorScreenState(
    val deck: Deck,
    val initialTab: DeckEditorScreenTab,
    typedDeckName: String = ""
) : FlowMaker<DeckEditorScreenState>() {
    var typedDeckName: String by flowMaker(typedDeckName)
    
    enum class DeckEditorScreenTab {
        Settings,
        Content
    }
}