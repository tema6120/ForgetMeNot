package com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.FileFormatDb
import com.odnovolov.forgetmenot.persistence.toFileFormat
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider

class FileImportStorageProvider(
    private val database: Database
) : LongTermStateProvider<FileImportStorage> {
    override fun load(): FileImportStorage {
        val customFileFormats = buildFileFormats()
        val keyValues: Map<Long, String?> = loadKeyValues()
        val lastUsedEncodingName = buildLastUsedEncodingName(keyValues)
        val lastUsedFormatForTxt = buildLastUsedFormatForTxt(keyValues, customFileFormats)
        val lastUsedFormatForCsv = buildLastUsedFormatForCsv(keyValues, customFileFormats)
        val lastUsedFormatForTsv = buildLastUsedFormatForTsv(keyValues, customFileFormats)
        return FileImportStorage(
            customFileFormats,
            lastUsedEncodingName,
            lastUsedFormatForTxt,
            lastUsedFormatForCsv,
            lastUsedFormatForTsv
        )
    }

    private fun buildLastUsedFormatForTxt(
        keyValues: Map<Long, String?>,
        customFileFormats: CopyableCollection<FileFormat>
    ): FileFormat {
        val fileFormatId = keyValues[DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TXT]
            ?.toLong()
            ?: return FileImportStorage.DEFAULT_FILE_FORMAT_FOR_TXT
        return FileFormat.predefinedFormats
            .find { fileFormat: FileFormat -> fileFormat.id == fileFormatId }
            ?: customFileFormats
                .find { fileFormat: FileFormat -> fileFormat.id == fileFormatId }
            ?: FileImportStorage.DEFAULT_FILE_FORMAT_FOR_TXT
    }

    private fun buildLastUsedFormatForCsv(
        keyValues: Map<Long, String?>,
        customFileFormats: CopyableCollection<FileFormat>
    ): FileFormat {
        val fileFormatId = keyValues[DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_CSV]
            ?.toLong()
            ?: return FileImportStorage.DEFAULT_FILE_FORMAT_FOR_CSV
        return FileFormat.predefinedFormats
            .find { fileFormat: FileFormat -> fileFormat.id == fileFormatId }
            ?: customFileFormats
                .find { fileFormat: FileFormat -> fileFormat.id == fileFormatId }
            ?: FileImportStorage.DEFAULT_FILE_FORMAT_FOR_CSV
    }

    private fun buildLastUsedFormatForTsv(
        keyValues: Map<Long, String?>,
        customFileFormats: CopyableCollection<FileFormat>
    ): FileFormat {
        val fileFormatId = keyValues[DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TSV]
            ?.toLong()
            ?: return FileImportStorage.DEFAULT_FILE_FORMAT_FOR_TSV
        return FileFormat.predefinedFormats
            .find { fileFormat: FileFormat -> fileFormat.id == fileFormatId }
            ?: customFileFormats
                .find { fileFormat: FileFormat -> fileFormat.id == fileFormatId }
            ?: FileImportStorage.DEFAULT_FILE_FORMAT_FOR_TSV
    }

    private fun buildLastUsedEncodingName(keyValues: Map<Long, String?>): String {
        return keyValues[DbKeys.LAST_USED_ENCODING_NAME]
            ?: FileImportStorage.DEFAULT_ENCODING_NAME
    }

    private fun loadKeyValues() = database.keyValueQueries
        .selectValues(
            keys = listOf(
                DbKeys.LAST_USED_ENCODING_NAME,
                DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TXT,
                DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_CSV,
                DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TSV
            )
        )
        .executeAsList()
        .associate { (key, value) -> key to value }

    private fun buildFileFormats(): CopyableCollection<FileFormat> {
        val fileFormatTable = database.fileFormatQueries.selectAll().executeAsList()
        return fileFormatTable.map { fileFormatDb: FileFormatDb -> fileFormatDb.toFileFormat() }
            .toCopyableList()
    }
}