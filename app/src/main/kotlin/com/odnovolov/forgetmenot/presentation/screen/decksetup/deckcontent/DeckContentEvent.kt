package com.odnovolov.forgetmenot.presentation.screen.decksetup.deckcontent

import java.io.OutputStream

sealed class DeckContentEvent {
    object ExportButtonClicked : DeckContentEvent()
    class OutputStreamOpened(val outputStream: OutputStream) : DeckContentEvent()
    object SearchButtonClicked : DeckContentEvent()
    class CardClicked(val cardId: Long) : DeckContentEvent()
    object AddCardButtonClicked : DeckContentEvent()
}