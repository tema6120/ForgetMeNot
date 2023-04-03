package com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImportStorage
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.FileFormatDb
import com.odnovolov.forgetmenot.persistence.toCardsFileFormat
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider

class CardsImportStorageProvider(
    private val database: Database
) : LongTermStateProvider<CardsImportStorage> {
    override fun load(): CardsImportStorage {
        val customFileFormats = buildFileFormats()
        val keyValues: Map<Long, String?> = loadKeyValues()
        val lastUsedEncodingName = buildLastUsedEncodingName(keyValues)
        val lastUsedFormatForTxt = buildLastUsedFormatForTxt(keyValues, customFileFormats)
        val lastUsedFormatForCsv = buildLastUsedFormatForCsv(keyValues, customFileFormats)
        val lastUsedFormatForTsv = buildLastUsedFormatForTsv(keyValues, customFileFormats)
        return CardsImportStorage(
            customFileFormats,
            lastUsedEncodingName,
            lastUsedFormatForTxt,
            lastUsedFormatForCsv,
            lastUsedFormatForTsv
        )
    }

    private fun buildLastUsedFormatForTxt(
        keyValues: Map<Long, String?>,
        customFileFormats: CopyableCollection<CardsFileFormat>
    ): CardsFileFormat {
        val fileFormatId = keyValues[DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TXT]
            ?.toLong()
            ?: return CardsImportStorage.DEFAULT_FILE_FORMAT_FOR_TXT
        return CardsFileFormat.predefinedFormats
            .find { fileFormat: CardsFileFormat -> fileFormat.id == fileFormatId }
            ?: customFileFormats
                .find { fileFormat: CardsFileFormat -> fileFormat.id == fileFormatId }
            ?: CardsImportStorage.DEFAULT_FILE_FORMAT_FOR_TXT
    }

    private fun buildLastUsedFormatForCsv(
        keyValues: Map<Long, String?>,
        customFileFormats: CopyableCollection<CardsFileFormat>
    ): CardsFileFormat {
        val fileFormatId = keyValues[DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_CSV]
            ?.toLong()
            ?: return CardsImportStorage.DEFAULT_FILE_FORMAT_FOR_CSV
        return CardsFileFormat.predefinedFormats
            .find { fileFormat: CardsFileFormat -> fileFormat.id == fileFormatId }
            ?: customFileFormats
                .find { fileFormat: CardsFileFormat -> fileFormat.id == fileFormatId }
            ?: CardsImportStorage.DEFAULT_FILE_FORMAT_FOR_CSV
    }

    private fun buildLastUsedFormatForTsv(
        keyValues: Map<Long, String?>,
        customFileFormats: CopyableCollection<CardsFileFormat>
    ): CardsFileFormat {
        val fileFormatId = keyValues[DbKeys.LAST_USED_FILE_FORMAT_ID_FOR_TSV]
            ?.toLong()
            ?: return CardsImportStorage.DEFAULT_FILE_FORMAT_FOR_TSV
        return CardsFileFormat.predefinedFormats
            .find { fileFormat: CardsFileFormat -> fileFormat.id == fileFormatId }
            ?: customFileFormats
                .find { fileFormat: CardsFileFormat -> fileFormat.id == fileFormatId }
            ?: CardsImportStorage.DEFAULT_FILE_FORMAT_FOR_TSV
    }

    private fun buildLastUsedEncodingName(keyValues: Map<Long, String?>): String {
        return keyValues[DbKeys.LAST_USED_ENCODING_NAME]
            ?: CardsImportStorage.DEFAULT_ENCODING_NAME
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

    private fun buildFileFormats(): CopyableCollection<CardsFileFormat> {
        val fileFormatTable = database.fileFormatQueries.selectAll().executeAsList()
        return fileFormatTable.map { fileFormatDb: FileFormatDb -> fileFormatDb.toCardsFileFormat() }
            .toCopyableList()
    }
}