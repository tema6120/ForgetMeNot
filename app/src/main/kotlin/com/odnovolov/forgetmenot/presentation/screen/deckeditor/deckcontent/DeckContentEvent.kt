package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import java.io.OutputStream

sealed class DeckContentEvent {
    object ExportButtonClicked : DeckContentEvent()
    class SelectedTheFileFormatForExport(val fileFormat: FileFormat) : DeckContentEvent()
    class OpenedTheOutputStream(val outputStream: OutputStream) : DeckContentEvent()
    object SearchButtonClicked : DeckContentEvent()
    class CardClicked(val cardId: Long) : DeckContentEvent()
}