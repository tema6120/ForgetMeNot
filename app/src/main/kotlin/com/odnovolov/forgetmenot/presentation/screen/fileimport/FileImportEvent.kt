package com.odnovolov.forgetmenot.presentation.screen.fileimport

sealed class FileImportEvent {
    class CardsFileIsOpened(val cardsFileId: Long) : FileImportEvent()
    object PreviousButtonClicked : FileImportEvent()
    object NextButtonClicked : FileImportEvent()
    object SkipButtonClicked : FileImportEvent()
    object DoneButtonClicked : FileImportEvent()
    object FixErrorsButtonClicked : FileImportEvent()
    object ImportIgnoringErrorsButtonClicked : FileImportEvent()
}