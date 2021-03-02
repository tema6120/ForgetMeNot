package com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar

sealed class CardSelectionEvent {
    object InvertOptionSelected : CardSelectionEvent()
    object ChangeGradeOptionSelected : CardSelectionEvent()
    object MarkAsLearnedOptionSelected : CardSelectionEvent()
    object MarkAsUnlearnedOptionSelected : CardSelectionEvent()
    object RemoveCardsOptionSelected : CardSelectionEvent()
    object MoveOptionSelected : CardSelectionEvent()
    object CopyOptionSelected : CardSelectionEvent()
}