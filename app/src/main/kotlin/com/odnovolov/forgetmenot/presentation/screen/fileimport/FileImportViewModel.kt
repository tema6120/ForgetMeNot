package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter

class FileImportViewModel(
    private val fileImporterState: FileImporter.State
) {
    val currentCardsFileId: Long get() = with(fileImporterState) {
        files[currentPosition].id
    }

    val errorsInfo: ErrorsInfo get() = with(fileImporterState) {
        val numberOfDecksContainingErrors: Int = files.count { it.errors.isNotEmpty() }
        val totalNumberOfErrors: Int = files.sumOf { it.errors.size }
        ErrorsInfo(numberOfDecksContainingErrors, totalNumberOfErrors)
    }

    data class ErrorsInfo(
        val numberOfDecksContainingErrors: Int,
        val totalNumberOfErrors: Int
    )
}