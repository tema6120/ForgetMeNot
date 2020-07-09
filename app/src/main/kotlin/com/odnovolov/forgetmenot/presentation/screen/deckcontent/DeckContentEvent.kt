package com.odnovolov.forgetmenot.presentation.screen.deckcontent

sealed class DeckContentEvent {
    class CardClicked(val cardId: Long) : DeckContentEvent()
    object AddCardButtonClicked : DeckContentEvent()
}