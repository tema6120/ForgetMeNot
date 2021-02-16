package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.*
import org.apache.commons.csv.CSVFormat
import java.lang.Exception

class DsvFormatController(
    private val screenState: DsvFormatScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DsvFormatEvent, Nothing>() {
    override fun handle(event: DsvFormatEvent) {
        when (event) {
            BackButtonClicked -> {
                navigator.navigateUp()
            }

            CancelButtonClicked -> {
                // todo
            }

            DoneButtonClicked -> {
                // todo
            }

            FormatNameButtonClicked -> {
                // todo
            }

            DeleteFormatButtonClicked -> {
                // todo
            }

            CloseTipButtonClicked -> {
                screenState.isTipVisible = false
            }

            is DelimiterChanged -> {
                screenState.delimiter = event.delimiter
                validate()
            }

            is TrailingDelimiterChanged -> {
                screenState.trailingDelimiter = event.trailingDelimiter
                validate()
            }

            is QuoteCharacterChanged -> {
                screenState.quoteCharacter = event.quoteCharacter
                validate()
            }

            is QuoteModeChanged -> {
                screenState.quoteMode = event.quoteMode
                validate()
            }

            is EscapeCharacterChanged -> {
                screenState.escapeCharacter = event.escapeCharacter
                validate()
            }

            is NullStringChanged -> {
                screenState.nullString = event.nullString
                validate()
            }

            is IgnoreSurroundingSpacesChanged -> {
                screenState.ignoreSurroundingSpaces = event.ignoreSurroundingSpaces
                validate()
            }

            is TrimChanged -> {
                screenState.trim = event.trim
                validate()
            }

            is IgnoreEmptyLinesChanged -> {
                screenState.ignoreEmptyLines = event.ignoreEmptyLines
                validate()
            }

            is RecordSeparatorChanged -> {
                screenState.recordSeparator = event.recordSeparator
                validate()
            }

            is CommentMarkerChanged -> {
                screenState.commentMarker = event.commentMarker
                validate()
            }

            is SkipHeaderRecordChanged -> {
                screenState.skipHeaderRecord = event.skipHeaderRecord
                validate()
            }

            is HeaderColumnNameChanged -> {
                val currentHeader = screenState.header
                when {
                    currentHeader == null -> {
                        if (event.columnName.isEmpty()) return
                        screenState.header = arrayOf(event.columnName)
                    }
                    event.position > currentHeader.lastIndex -> {
                        screenState.header = Array(currentHeader.size + 1) { i: Int ->
                            if (i <= currentHeader.lastIndex) {
                                currentHeader[i]
                            } else {
                                event.columnName
                            }
                        }
                    }
                    event.position == currentHeader.lastIndex && event.columnName.isEmpty() -> {
                        screenState.header =
                            if (currentHeader.size <= 1) {
                                null
                            } else {
                                Array(currentHeader.size - 1) { i: Int ->
                                    currentHeader[i]
                                }
                            }
                    }
                    else -> {
                        screenState.header!![event.position] = event.columnName
                    }
                }
                validate()
            }

            is IgnoreHeaderCaseChanged -> {
                screenState.ignoreHeaderCase = event.ignoreHeaderCase
                validate()
            }

            is AllowDuplicateHeaderNamesChanged -> {
                screenState.allowDuplicateHeaderNames = event.allowDuplicateHeaderNames
                validate()
            }

            is AllowMissingColumnNamesChanged -> {
                screenState.allowMissingColumnNames = event.allowMissingColumnNames
                validate()
            }

            is HeaderCommentChanged -> {
                val currentHeaderComments = screenState.headerComments
                when {
                    currentHeaderComments == null -> {
                        if (event.headerComment.isEmpty()) return
                        screenState.headerComments = arrayOf(event.headerComment)
                    }
                    event.position > currentHeaderComments.lastIndex -> {
                        screenState.headerComments =
                            Array(currentHeaderComments.size + 1) { i: Int ->
                                if (i <= currentHeaderComments.lastIndex) {
                                    currentHeaderComments[i]
                                } else {
                                    event.headerComment
                                }
                            }
                    }
                    event.position == currentHeaderComments.lastIndex
                            && event.headerComment.isEmpty() -> {
                        screenState.headerComments =
                            if (currentHeaderComments.size <= 1) {
                                null
                            } else {
                                Array(currentHeaderComments.size - 1) { i: Int ->
                                    currentHeaderComments[i]
                                }
                            }
                    }
                    else -> {
                        screenState.headerComments!![event.position] = event.headerComment
                    }
                }
                validate()
            }

            is AutoFlushChanged -> {
                screenState.autoFlush = event.autoFlush
                validate()
            }
        }
    }

    private fun validate() {
        screenState.errorMessage = try {
            with(screenState) {
                CSVFormat.newFormat(delimiter!!)
                    .withTrailingDelimiter(trailingDelimiter)
                    .withQuote(quoteCharacter)
                    .withQuoteMode(quoteMode)
                    .withEscape(escapeCharacter)
                    .withNullString(nullString)
                    .withIgnoreSurroundingSpaces(ignoreSurroundingSpaces)
                    .withTrim(trim)
                    .withIgnoreEmptyLines(ignoreEmptyLines)
                    .withRecordSeparator(recordSeparator)
                    .withCommentMarker(commentMarker)
                    .withSkipHeaderRecord(skipHeaderRecord)
                    .apply { header?.let(::withHeader) }
                    .withIgnoreHeaderCase(ignoreHeaderCase)
                    .withAllowDuplicateHeaderNames(allowDuplicateHeaderNames)
                    .withAllowMissingColumnNames(allowMissingColumnNames)
                    .withHeaderComments(headerComments)
                    .withAutoFlush(autoFlush)
            }
            null
        } catch (e: Exception) {
            e.message ?: e::class.java.simpleName
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}