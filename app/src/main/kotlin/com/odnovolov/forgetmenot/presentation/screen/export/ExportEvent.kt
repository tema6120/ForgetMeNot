package com.odnovolov.forgetmenot.presentation.screen.export

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import java.io.OutputStream

sealed class ExportEvent {
    class SelectedTheFileFormat(val fileFormat: FileFormat) : ExportEvent()
    class GotFilesCreationResult(val filesCreationResult: List<FileCreationResult>) : ExportEvent() {
        data class FileCreationResult(val deckName: String, val outputStream: OutputStream?)
    }
}