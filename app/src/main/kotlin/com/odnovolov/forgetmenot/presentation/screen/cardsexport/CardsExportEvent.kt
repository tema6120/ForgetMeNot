package com.odnovolov.forgetmenot.presentation.screen.cardsexport

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import java.io.OutputStream

sealed class CardsExportEvent {
    class SelectedTheFileFormat(val fileFormat: CardsFileFormat) : CardsExportEvent()
    class GotFilesCreationResult(val filesCreationResult: List<FileCreationResult>) : CardsExportEvent() {
        data class FileCreationResult(val deckName: String, val outputStream: OutputStream?)
    }
}