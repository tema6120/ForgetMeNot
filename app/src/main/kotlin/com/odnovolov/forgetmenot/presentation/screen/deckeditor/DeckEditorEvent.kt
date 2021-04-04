package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class DeckEditorEvent {
    object RenameDeckButtonClicked : DeckEditorEvent()
    object AddCardButtonClicked : DeckEditorEvent()

    // Card selection toolbar
    object CancelledCardSelection : DeckEditorEvent()
    object SelectAllCardsButtonClicked : DeckEditorEvent()
    object InvertCardSelectionOptionWasSelected : DeckEditorEvent()
    object ChangeGradeCardSelectionOptionWasSelected : DeckEditorEvent()
    class GradeWasSelected(val grade: Int) : DeckEditorEvent()
    object MarkAsLearnedCardSelectionOptionWasSelected : DeckEditorEvent()
    object MarkAsUnlearnedCardSelectionOptionWasSelected : DeckEditorEvent()
    object RemoveCardsCardSelectionOptionWasSelected : DeckEditorEvent()
    object MoveCardSelectionOptionWasSelected : DeckEditorEvent()
    class DeckToMoveCardsToWasSelected(val abstractDeck: AbstractDeck) : DeckEditorEvent()
    object CopyCardSelectionOptionWasSelected : DeckEditorEvent()
    class DeckToCopyCardsToWasSelected(val abstractDeck: AbstractDeck) : DeckEditorEvent()
    object CancelSnackbarButtonClicked : DeckEditorEvent()
}