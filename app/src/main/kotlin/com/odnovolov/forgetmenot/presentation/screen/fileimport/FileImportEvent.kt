package com.odnovolov.forgetmenot.presentation.screen.fileimport

sealed class FileImportEvent {
    class CardsFileWasOpened(val cardsFileId: Long) : FileImportEvent()
    object PreviousButtonClicked : FileImportEvent()
    object NextButtonClicked : FileImportEvent()
    object SkipButtonClicked : FileImportEvent()
    object CancelButtonClicked : FileImportEvent()
    object DoneButtonClicked : FileImportEvent()
    object FixErrorsButtonClicked : FileImportEvent()
    object ImportIgnoringErrorsButtonClicked : FileImportEvent()
    object BackButtonClicked : FileImportEvent()
    object UserConfirmedExit : FileImportEvent()
}