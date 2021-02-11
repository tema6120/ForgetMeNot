package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter

class FileImportViewModel(
    private val fileImporterState: FileImporter.State
) {
    val currentCardsFileId: Long get() = with(fileImporterState) {
        files[currentPosition].id
    }
}