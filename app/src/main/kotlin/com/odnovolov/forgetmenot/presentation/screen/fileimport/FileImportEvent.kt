package com.odnovolov.forgetmenot.presentation.screen.fileimport

sealed class FileImportEvent {
    object CancelButtonClicked : FileImportEvent()
    object DoneButtonClicked : FileImportEvent()
    object RenameDeckButtonClicked : FileImportEvent()
    object AddCardsToNewDeckButtonClicked : FileImportEvent()
    object AddCardsToExistingDeckButtonClicked : FileImportEvent()
    class TextChanged(val newText: String) : FileImportEvent()
}