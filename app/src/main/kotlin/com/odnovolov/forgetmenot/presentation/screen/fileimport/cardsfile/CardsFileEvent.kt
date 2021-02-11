package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile

import com.odnovolov.forgetmenot.domain.entity.Deck

sealed class CardsFileEvent {
    object RenameDeckButtonClicked : CardsFileEvent()
    object AddCardsToNewDeckButtonClicked : CardsFileEvent()
    class SubmittedNameForNewDeck(val deckName: String) : CardsFileEvent()
    object AddCardsToExistingDeckButtonClicked : CardsFileEvent()
    class TargetDeckIsSelected(val deck: Deck) : CardsFileEvent()
}