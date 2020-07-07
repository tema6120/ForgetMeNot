package com.odnovolov.forgetmenot.presentation.screen.cardseditor

sealed class CardsEditorEvent {
    class PageSelected(val position: Int) : CardsEditorEvent()
    object CancelButtonClicked : CardsEditorEvent()
    object AcceptButtonClicked : CardsEditorEvent()
}