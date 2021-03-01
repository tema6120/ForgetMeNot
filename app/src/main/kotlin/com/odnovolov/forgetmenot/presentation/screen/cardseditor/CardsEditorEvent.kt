package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class CardsEditorEvent {
    class PageSelected(val position: Int) : CardsEditorEvent()
    class GradeWasChanged(val grade: Int) : CardsEditorEvent()
    object MarkAsLearnedButtonClicked : CardsEditorEvent()
    object MarkAsUnlearnedButtonClicked : CardsEditorEvent()
    object RemoveCardButtonClicked : CardsEditorEvent()
    object RestoreLastRemovedCardButtonClicked : CardsEditorEvent()
    object MoveCardButtonClicked : CardsEditorEvent()
    class DeckToMoveCardToIsSelected(val abstractDeck: AbstractDeck) : CardsEditorEvent()
    object CancelLastMovementButtonClicked : CardsEditorEvent()
    object CopyCardButtonClicked : CardsEditorEvent()
    class DeckToCopyCardToIsSelected(val abstractDeck: AbstractDeck) : CardsEditorEvent()
    object CancelLastCopyingButtonClicked : CardsEditorEvent()
    object HelpButtonClicked : CardsEditorEvent()
    object CancelButtonClicked : CardsEditorEvent()
    object DoneButtonClicked : CardsEditorEvent()
    object BackButtonClicked : CardsEditorEvent()
    object SaveButtonClicked : CardsEditorEvent()
    object UserConfirmedExit : CardsEditorEvent()
}