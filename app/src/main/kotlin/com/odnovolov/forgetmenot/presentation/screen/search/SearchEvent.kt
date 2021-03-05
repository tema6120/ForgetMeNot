package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class SearchEvent {
    class SearchTextChanged(val text: String) : SearchEvent()
    class CardClicked(val selectableSearchCard: SelectableSearchCard) : SearchEvent()
    class CardLongClicked(val selectableSearchCard: SelectableSearchCard) : SearchEvent()

    // Card selection toolbar
    object CancelledCardSelection : SearchEvent()
    object SelectAllCardsButtonClicked : SearchEvent()
    object InvertCardSelectionOptionSelected : SearchEvent()
    object ChangeGradeCardSelectionOptionSelected : SearchEvent()
    class SelectedGrade(val grade: Int) : SearchEvent()
    object MarkAsLearnedCardSelectionOptionSelected : SearchEvent()
    object MarkAsUnlearnedCardSelectionOptionSelected : SearchEvent()
    object RemoveCardsCardSelectionOptionSelected : SearchEvent()
    object MoveCardSelectionOptionSelected : SearchEvent()
    class DeckToMoveCardsToIsSelected(val abstractDeck: AbstractDeck) : SearchEvent()
    object CopyCardSelectionOptionSelected : SearchEvent()
    class DeckToCopyCardsToIsSelected(val abstractDeck: AbstractDeck) : SearchEvent()
    object CancelSnackbarButtonClicked : SearchEvent()
}