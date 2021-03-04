package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class DeckEditorEvent {
    object RenameDeckButtonClicked : DeckEditorEvent()
    object AddCardButtonClicked : DeckEditorEvent()

    // Card selection toolbar
    object CancelledCardSelection : DeckEditorEvent()
    object SelectAllCardsButtonClicked : DeckEditorEvent()
    object InvertCardSelectionOptionSelected : DeckEditorEvent()
    object ChangeGradeCardSelectionOptionSelected : DeckEditorEvent()
    class SelectedGrade(val grade: Int) : DeckEditorEvent()
    object MarkAsLearnedCardSelectionOptionSelected : DeckEditorEvent()
    object MarkAsUnlearnedCardSelectionOptionSelected : DeckEditorEvent()
    object RemoveCardsCardSelectionOptionSelected : DeckEditorEvent()
    object MoveCardSelectionOptionSelected : DeckEditorEvent()
    class DeckToMoveCardsToIsSelected(val abstractDeck: AbstractDeck) : DeckEditorEvent()
    object CopyCardSelectionOptionSelected : DeckEditorEvent()
    class DeckToCopyCardsToIsSelected(val abstractDeck: AbstractDeck) : DeckEditorEvent()
    object CancelSnackbarButtonClicked : DeckEditorEvent()
}