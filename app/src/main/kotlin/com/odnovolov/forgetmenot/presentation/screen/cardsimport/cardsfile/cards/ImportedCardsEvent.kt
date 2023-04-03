package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards

sealed class ImportedCardsEvent {
    class CardClicked(val id: Long) : ImportedCardsEvent()
    object SelectAllButtonClicked : ImportedCardsEvent()
    object UnselectAllButtonClicked : ImportedCardsEvent()
    object SelectOnlyNewButtonClicked : ImportedCardsEvent()
}