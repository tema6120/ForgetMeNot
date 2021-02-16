package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.domain.interactor.fileimport.DsvFormatEditor
import com.odnovolov.forgetmenot.domain.interactor.fileimport.DsvFormatEditor.SaveResult
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatController.Command
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatController.Command.ShowSaveErrorMessage
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.*
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatScreenState.Purpose

class DsvFormatController(
    private val dsvFormatEditor: DsvFormatEditor,
    private val fileImporter: FileImporter,
    private val screenState: DsvFormatScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DsvFormatEvent, Command>() {
    sealed class Command {
        class ShowSaveErrorMessage(val cause: SaveResult.Failure.Cause) : Command()
    }

    override fun handle(event: DsvFormatEvent) {
        when (event) {
            BackButtonClicked, CancelButtonClicked -> {
                if (screenState.purpose != Purpose.View && dsvFormatEditor.isChanged()) {
                    // todo: ask to quit
                } else {
                    navigator.navigateUp()
                }
            }

            DoneButtonClicked -> {
                when (val result: SaveResult = dsvFormatEditor.save()) {
                    SaveResult.Success -> {
                        if (screenState.purpose == Purpose.CreateNew) {
                            val createdFileFormat = dsvFormatEditor.state.editingFileFormat
                            fileImporter.setFormat(createdFileFormat)
                        }
                        navigator.navigateUp()
                    }
                    is SaveResult.Failure -> {
                        sendCommand(ShowSaveErrorMessage(result.cause))
                    }
                }
            }

            is FormatNameChanged -> {
                dsvFormatEditor.setFormatName(event.formatName)
            }

            DeleteFormatButtonClicked -> {
                val success = dsvFormatEditor.remove()
                // todo: check if it is used
                if (success) {
                    navigator.navigateUp()
                }
            }

            CloseTipButtonClicked -> {
                screenState.isTipVisible = false
            }

            is DelimiterChanged -> {
                dsvFormatEditor.setDelimiter(event.delimiter)
            }

            is TrailingDelimiterChanged -> {
                dsvFormatEditor.setTrailingDelimiter(event.trailingDelimiter)
            }

            is QuoteCharacterChanged -> {
                dsvFormatEditor.setQuoteCharacter(event.quoteCharacter)
            }

            is QuoteModeChanged -> {
                dsvFormatEditor.setQuoteMode(event.quoteMode)
            }

            is EscapeCharacterChanged -> {
                dsvFormatEditor.setEscapeCharacter(event.escapeCharacter)
            }

            is NullStringChanged -> {
                dsvFormatEditor.setNullString(event.nullString)
            }

            is IgnoreSurroundingSpacesChanged -> {
                dsvFormatEditor.setIgnoreSurroundingSpaces(event.ignoreSurroundingSpaces)
            }

            is TrimChanged -> {
                dsvFormatEditor.setTrim(event.trim)
            }

            is IgnoreEmptyLinesChanged -> {
                dsvFormatEditor.setIgnoreEmptyLines(event.ignoreEmptyLines)
            }

            is RecordSeparatorChanged -> {
                dsvFormatEditor.setRecordSeparator(event.recordSeparator)
            }

            is CommentMarkerChanged -> {
                dsvFormatEditor.setCommentMarker(event.commentMarker)
            }

            is SkipHeaderRecordChanged -> {
                dsvFormatEditor.setSkipHeaderRecord(event.skipHeaderRecord)
            }

            is HeaderColumnNameChanged -> {
                dsvFormatEditor.setHeaderColumnName(event.position, event.columnName)
            }

            is IgnoreHeaderCaseChanged -> {
                dsvFormatEditor.setIgnoreHeaderCase(event.ignoreHeaderCase)
            }

            is AllowDuplicateHeaderNamesChanged -> {
                dsvFormatEditor.setAllowDuplicateHeaderNames(event.allowDuplicateHeaderNames)
            }

            is AllowMissingColumnNamesChanged -> {
                dsvFormatEditor.setAllowMissingColumnNames(event.allowMissingColumnNames)
            }

            is HeaderCommentChanged -> {
                dsvFormatEditor.setHeaderComment(event.position, event.headerComment)
            }

            is AutoFlushChanged -> {
                dsvFormatEditor.setAutoFlush(event.autoFlush)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}