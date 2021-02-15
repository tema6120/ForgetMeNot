package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.*

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
            }

            is TrailingDelimiterChanged -> {
                screenState.trailingDelimiter = event.trailingDelimiter
            }

            is QuoteCharacterChanged -> {
                screenState.quoteCharacter = event.quoteCharacter
            }

            is QuoteModeChanged -> {
                screenState.quoteMode = event.quoteMode
            }

            is EscapeCharacterChanged -> {
                screenState.escapeCharacter = event.escapeCharacter
            }

            is NullStringChanged -> {
                screenState.nullString = event.nullString
            }

            is IgnoreSurroundingSpacesChanged -> {
                screenState.ignoreSurroundingSpaces = event.ignoreSurroundingSpaces
            }

            is TrimChanged -> {
                screenState.trim = event.trim
            }

            is IgnoreEmptyLinesChanged -> {
                screenState.ignoreEmptyLines = event.ignoreEmptyLines
            }

            is RecordSeparatorChanged -> {
                screenState.recordSeparator = event.recordSeparator
            }

            is CommentMarkerChanged -> {
                screenState.commentMarker = event.commentMarker
            }

            is SkipHeaderRecordChanged -> {
                screenState.skipHeaderRecord = event.skipHeaderRecord
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
            }

            is IgnoreHeaderCaseChanged -> {
                screenState.ignoreHeaderCase = event.ignoreHeaderCase
            }

            is AllowDuplicateHeaderNamesChanged -> {
                screenState.allowDuplicateHeaderNames = event.allowDuplicateHeaderNames
            }

            is AllowMissingColumnNamesChanged -> {
                screenState.allowMissingColumnNames = event.allowMissingColumnNames
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
            }

            is AutoFlushChanged -> {
                screenState.autoFlush = event.autoFlush
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}