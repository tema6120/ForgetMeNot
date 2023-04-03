package com.odnovolov.forgetmenot.domain.interactor.cardsimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class CardsImportStorage(
    customFileFormats: CopyableCollection<CardsFileFormat>,
    lastUsedEncodingName: String,
    lastUsedFormatForTxt: CardsFileFormat,
    lastUsedFormatForCsv: CardsFileFormat,
    lastUsedFormatForTsv: CardsFileFormat
) : FlowMakerWithRegistry<CardsImportStorage>() {
    var customFileFormats: CopyableCollection<CardsFileFormat>
            by flowMakerForCopyableCollection(customFileFormats)

    var lastUsedEncodingName: String by flowMaker(lastUsedEncodingName)
    var lastUsedFormatForTxt: CardsFileFormat by flowMakerForCopyable(lastUsedFormatForTxt)
    var lastUsedFormatForCsv: CardsFileFormat by flowMakerForCopyable(lastUsedFormatForCsv)
    var lastUsedFormatForTsv: CardsFileFormat by flowMakerForCopyable(lastUsedFormatForTsv)

    override fun copy() = CardsImportStorage(
        customFileFormats,
        lastUsedEncodingName,
        lastUsedFormatForTxt,
        lastUsedFormatForCsv,
        lastUsedFormatForTsv
    )

    companion object {
        const val DEFAULT_ENCODING_NAME = "UTF-8"
        val DEFAULT_FILE_FORMAT_FOR_TXT by lazy { CardsFileFormat.FMN_FORMAT }
        val DEFAULT_FILE_FORMAT_FOR_CSV by lazy { CardsFileFormat.CSV_DEFAULT }
        val DEFAULT_FILE_FORMAT_FOR_TSV by lazy { CardsFileFormat.CSV_TDF }
    }
}