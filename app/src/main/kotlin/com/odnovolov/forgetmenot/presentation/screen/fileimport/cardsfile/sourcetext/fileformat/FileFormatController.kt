package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.domain.interactor.fileimport.CsvParser
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatDiScope
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatScreenState
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat.FileFormatEvent.*
import org.apache.commons.csv.CSVFormat

class FileFormatController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<FileFormatEvent, Nothing>() {
    override fun handle(event: FileFormatEvent) {
        when (event) {
            is FileFormatRadioButtonClicked -> {
                fileImporter.setFormat(event.fileFormat)
            }

            is ViewFileFormatSettingsButtonClicked -> {
                navigator.navigateToDsvFormat {
                    val parser = event.fileFormat.parser
                    val csvFormat = if (parser is CsvParser) parser.csvFormat else CSVFormat.DEFAULT
                    val screenState: DsvFormatScreenState =
                        createDsvFormatScreenState(event.fileFormat, csvFormat)
                    DsvFormatDiScope.create(screenState)
                }
            }

            is EditFileFormatSettingsButtonClicked -> {
                /*navigator.navigateToDsvFormat {
                    DsvFormatDiScope()
                }*/
            }
        }
    }

    private fun createDsvFormatScreenState(
        fileFormat: FileFormat,
        csvFormat: CSVFormat
    ) = DsvFormatScreenState(
        isReadOnly = fileFormat.isPredefined,
        fileFormat.name,
        isTipVisible = true,
        errorMessage = null,
        csvFormat.delimiter,
        csvFormat.trailingDelimiter,
        csvFormat.quoteCharacter,
        csvFormat.quoteMode,
        csvFormat.escapeCharacter,
        csvFormat.nullString,
        csvFormat.ignoreSurroundingSpaces,
        csvFormat.trim,
        csvFormat.ignoreEmptyLines,
        csvFormat.recordSeparator,
        csvFormat.commentMarker,
        csvFormat.skipHeaderRecord,
        csvFormat.header,
        csvFormat.ignoreHeaderCase,
        csvFormat.allowDuplicateHeaderNames,
        csvFormat.allowMissingColumnNames,
        csvFormat.headerComments,
        csvFormat.autoFlush
    )

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}