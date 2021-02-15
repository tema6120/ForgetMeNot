package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import org.apache.commons.csv.QuoteMode

class DsvFormatScreenState(
    isReadOnly: Boolean,
    formatName: String,
    isTipVisible: Boolean,
    errorMessage: String?,
    delimiter: Char,
    trailingDelimiter: Boolean,
    quoteCharacter: Char?,
    quoteMode: QuoteMode?,
    escapeCharacter: Char?,
    nullString: String?,
    ignoreSurroundingSpaces: Boolean,
    trim: Boolean,
    ignoreEmptyLines: Boolean,
    recordSeparator: String?,
    commentMarker: Char?,
    skipHeaderRecord: Boolean,
    header: Array<String>?,
    ignoreHeaderCase: Boolean,
    allowDuplicateHeaderNames: Boolean,
    allowMissingColumnNames: Boolean,
    headerComments: Array<String>?,
    autoFlush: Boolean,
) : FlowMaker<DsvFormatScreenState>() {
    val isReadOnly: Boolean by flowMaker(isReadOnly)
    var formatName: String by flowMaker(formatName)
    var isTipVisible: Boolean by flowMaker(isTipVisible)
    var errorMessage: String? by flowMaker(errorMessage)
    var delimiter: Char? by flowMaker(delimiter)
    var trailingDelimiter: Boolean by flowMaker(trailingDelimiter)
    var quoteCharacter: Char? by flowMaker(quoteCharacter)
    var quoteMode: QuoteMode? by flowMaker(quoteMode)
    var escapeCharacter: Char? by flowMaker(escapeCharacter)
    var nullString: String? by flowMaker(nullString)
    var ignoreSurroundingSpaces: Boolean by flowMaker(ignoreSurroundingSpaces)
    var trim: Boolean by flowMaker(trim)
    var ignoreEmptyLines: Boolean by flowMaker(ignoreEmptyLines)
    var recordSeparator: String? by flowMaker(recordSeparator)
    var commentMarker: Char? by flowMaker(commentMarker)
    var skipHeaderRecord: Boolean by flowMaker(skipHeaderRecord)
    var header: Array<String>? by flowMaker(header)
    var ignoreHeaderCase: Boolean by flowMaker(ignoreHeaderCase)
    var allowDuplicateHeaderNames: Boolean by flowMaker(allowDuplicateHeaderNames)
    var allowMissingColumnNames: Boolean by flowMaker(allowMissingColumnNames)
    var headerComments: Array<String>? by flowMaker(headerComments)//
    var autoFlush: Boolean by flowMaker(autoFlush)
}