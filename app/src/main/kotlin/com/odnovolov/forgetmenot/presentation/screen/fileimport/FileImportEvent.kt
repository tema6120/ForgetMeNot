package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.Deck
import java.nio.charset.Charset

sealed class FileImportEvent {
    object CancelButtonClicked : FileImportEvent()
    object DoneButtonClicked : FileImportEvent()
    object RenameDeckButtonClicked : FileImportEvent()
    object AddCardsToNewDeckButtonClicked : FileImportEvent()
    class SubmittedNameForNewDeck(val deckName: String) : FileImportEvent()
    object AddCardsToExistingDeckButtonClicked : FileImportEvent()
    class TargetDeckIsSelected(val deck: Deck) : FileImportEvent()
}