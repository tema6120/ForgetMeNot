package com.odnovolov.forgetmenot.presentation.screen.fileimport

sealed class FileImportEvent {
    object CancelButtonClicked : FileImportEvent()
    object DoneButtonClicked : FileImportEvent()
    class TextChanged(val newText: String) : FileImportEvent()
}