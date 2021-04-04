package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck

sealed class SearchEvent {
    class SearchTextChanged(val text: String) : SearchEvent()
    class CardClicked(val cardId: Long) : SearchEvent()
    class CardLongClicked(val cardId: Long) : SearchEvent()

    // Card selection toolbar
    object CardSelectionWasCancelled : SearchEvent()
    object SelectAllCardsButtonClicked : SearchEvent()
    object InvertCardSelectionOptionWasSelected : SearchEvent()
    object ChangeGradeCardSelectionOptionWasSelected : SearchEvent()
    class GradeWasSelected(val grade: Int) : SearchEvent()
    object MarkAsLearnedCardSelectionOptionWasSelected : SearchEvent()
    object MarkAsUnlearnedCardSelectionOptionWasSelected : SearchEvent()
    object RemoveCardsCardSelectionOptionWasSelected : SearchEvent()
    object MoveCardSelectionOptionWasSelected : SearchEvent()
    class DeckToMoveCardsToWasSelected(val abstractDeck: AbstractDeck) : SearchEvent()
    object CopyCardSelectionOptionWasSelected : SearchEvent()
    class DeckToCopyCardsToWasSelected(val abstractDeck: AbstractDeck) : SearchEvent()
    object CancelSnackbarButtonClicked : SearchEvent()
}