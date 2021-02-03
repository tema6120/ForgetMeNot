package com.odnovolov.forgetmenot.domain.interactor.fileimport

data class ImportedFile(
    val fileName: String,
    val content: ByteArray
)