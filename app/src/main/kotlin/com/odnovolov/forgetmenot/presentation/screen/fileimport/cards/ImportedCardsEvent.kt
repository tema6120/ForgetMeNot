package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

sealed class ImportedCardsEvent {
    class CardClicked(val id: Long) : ImportedCardsEvent()
    object SelectAllButtonClicked : ImportedCardsEvent()
}