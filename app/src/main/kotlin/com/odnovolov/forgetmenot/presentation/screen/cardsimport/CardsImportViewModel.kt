package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter

class CardsImportViewModel(
    private val cardsImporterState: CardsImporter.State
) {
    val currentCardsFileId: Long get() = with(cardsImporterState) {
        files[currentPosition].id
    }

    val errorsInfo: ErrorsInfo get() = with(cardsImporterState) {
        val numberOfDecksContainingErrors: Int = files.count { it.errors.isNotEmpty() }
        val totalNumberOfErrors: Int = files.sumOf { it.errors.size }
        ErrorsInfo(numberOfDecksContainingErrors, totalNumberOfErrors)
    }

    data class ErrorsInfo(
        val numberOfDecksContainingErrors: Int,
        val totalNumberOfErrors: Int
    )
}