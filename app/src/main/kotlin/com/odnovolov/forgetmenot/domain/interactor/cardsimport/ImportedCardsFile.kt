package com.odnovolov.forgetmenot.domain.interactor.cardsimport

data class ImportedCardsFile(
    val fileName: String,
    val content: ByteArray
)