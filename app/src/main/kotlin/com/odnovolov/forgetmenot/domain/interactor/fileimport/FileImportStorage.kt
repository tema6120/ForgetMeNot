package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class FileImportStorage(
    customFileFormats: CopyableCollection<FileFormat>,
    lastUsedEncodingName: String,
    lastUsedFormatForTxt: FileFormat,
    lastUsedFormatForCsv: FileFormat,
    lastUsedFormatForTsv: FileFormat
) : FlowMakerWithRegistry<FileImportStorage>() {
    var customFileFormats: CopyableCollection<FileFormat>
            by flowMakerForCopyableCollection(customFileFormats)

    var lastUsedEncodingName: String by flowMaker(lastUsedEncodingName)
    var lastUsedFormatForTxt: FileFormat by flowMakerForCopyable(lastUsedFormatForTxt)
    var lastUsedFormatForCsv: FileFormat by flowMakerForCopyable(lastUsedFormatForCsv)
    var lastUsedFormatForTsv: FileFormat by flowMakerForCopyable(lastUsedFormatForTsv)

    override fun copy() = FileImportStorage(
        customFileFormats,
        lastUsedEncodingName,
        lastUsedFormatForTxt,
        lastUsedFormatForCsv,
        lastUsedFormatForTsv
    )

    companion object {
        const val DEFAULT_ENCODING_NAME = "UTF-8"
        val DEFAULT_FILE_FORMAT_FOR_TXT by lazy { FileFormat.FMN_FORMAT }
        val DEFAULT_FILE_FORMAT_FOR_CSV by lazy { FileFormat.CSV_DEFAULT }
        val DEFAULT_FILE_FORMAT_FOR_TSV by lazy { FileFormat.CSV_TDF }
    }
}