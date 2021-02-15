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

            is HeaderChanged -> {
                screenState.header = event.header
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

            is HeaderCommentsChanged -> {
                screenState.headerComments = event.headerComments
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