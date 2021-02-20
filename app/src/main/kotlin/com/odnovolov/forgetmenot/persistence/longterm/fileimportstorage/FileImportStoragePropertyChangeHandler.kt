package com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.FileFormatDb
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.toFileFormatDb

class FileImportStoragePropertyChangeHandler(
    private val database: Database
) : PropertyChangeHandler {
    override fun handle(change: Change) {
        when (change.property) {
            FileImportStorage::customFileFormats -> {
                if (change !is CollectionChange) return

                val removedFileFormats = change.removedItems as Collection<FileFormat>
                removedFileFormats.forEach { fileFormat: FileFormat ->
                    database.fileFormatQueries.delete(fileFormat.id)
                }

                val addedFileFormats = change.addedItems as Collection<FileFormat>
                addedFileFormats.forEach { fileFormat: FileFormat ->
                    val fileFormatDb: FileFormatDb = fileFormat.toFileFormatDb()
                    database.fileFormatQueries.insert(fileFormatDb)
                }
            }
            FileImportStorage::lastUsedEncodingName -> {
                if (change !is PropertyValueChange) return
                val lastUsedEncodingName = change.newValue as String
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_ENCODING_NAME,
                    value = lastUsedEncodingName
                )
            }
            FileImportStorage::lastUsedFormatForTxt -> {
                if (change !is PropertyValueChange) return
                val lastUsedFormatForTxt = change.newValue as FileFormat
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TXT,
                    value = lastUsedFormatForTxt.id.toString()
                )
            }
            FileImportStorage::lastUsedFormatForCsv -> {
                if (change !is PropertyValueChange) return
                val lastUsedFormatForCsv = change.newValue as FileFormat
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_CSV,
                    value = lastUsedFormatForCsv.id.toString()
                )
            }
            FileImportStorage::lastUsedFormatForTsv -> {
                if (change !is PropertyValueChange) return
                val lastUsedFormatForTsv = change.newValue as FileFormat
                database.keyValueQueries.replace(
                    key = DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TSV,
                    value = lastUsedFormatForTsv.id.toString()
                )
            }
        }
    }
}