package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

sealed class DeckContentEvent {
    object ExportButtonClicked : DeckContentEvent()
    object SearchButtonClicked : DeckContentEvent()
    class CardClicked(val cardId: Long) : DeckContentEvent()
}