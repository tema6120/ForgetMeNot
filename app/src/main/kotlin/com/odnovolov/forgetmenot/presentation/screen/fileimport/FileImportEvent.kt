package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.Deck

sealed class FileImportEvent {
    object CancelButtonClicked : FileImportEvent()
    object DoneButtonClicked : FileImportEvent()
    object RenameDeckButtonClicked : FileImportEvent()
    object AddCardsToNewDeckButtonClicked : FileImportEvent()
    object AddCardsToExistingDeckButtonClicked : FileImportEvent()
    class TargetDeckIsSelected(val deck: Deck) : FileImportEvent()
    class TextChanged(val newText: String) : FileImportEvent()
}