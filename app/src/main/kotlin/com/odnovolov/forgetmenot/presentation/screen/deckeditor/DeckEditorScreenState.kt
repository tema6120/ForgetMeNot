package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import kotlinx.serialization.Serializable

data class DeckEditorScreenState(
    val deck: Deck,
    val tabs: DeckEditorTabs,
    var fileFormatForExport: FileFormat? = null
)

@Serializable
sealed class DeckEditorTabs {
    @Serializable
    data class All(val initialTab: DeckEditorScreenTab) : DeckEditorTabs()

    @Serializable
    object OnlyDeckSettings : DeckEditorTabs()
}

enum class DeckEditorScreenTab {
    Settings,
    Content
}