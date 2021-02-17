package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import org.apache.commons.csv.QuoteMode

sealed class DsvFormatEvent {
    object BackButtonClicked : DsvFormatEvent()
    object CancelButtonClicked : DsvFormatEvent()
    object DoneButtonClicked : DsvFormatEvent()
    class FormatNameChanged(val formatName: String) : DsvFormatEvent()
    object DeleteFormatButtonClicked : DsvFormatEvent()
    object DeleteFormatPositiveDialogButtonClicked : DsvFormatEvent()
    object CloseTipButtonClicked : DsvFormatEvent()
    object SaveButtonClicked : DsvFormatEvent()
    object UserConfirmedExit : DsvFormatEvent()
    class DelimiterChanged(val delimiter: Char?) : DsvFormatEvent()
    class TrailingDelimiterChanged(val trailingDelimiter: Boolean) : DsvFormatEvent()
    class QuoteCharacterChanged(val quoteCharacter: Char?) : DsvFormatEvent()
    class QuoteModeChanged(val quoteMode: QuoteMode?) : DsvFormatEvent()
    class EscapeCharacterChanged(val escapeCharacter: Char?) : DsvFormatEvent()
    class NullStringChanged(val nullString: String?) : DsvFormatEvent()
    class IgnoreSurroundingSpacesChanged(val ignoreSurroundingSpaces: Boolean) : DsvFormatEvent()
    class TrimChanged(val trim: Boolean) : DsvFormatEvent()
    class IgnoreEmptyLinesChanged(val ignoreEmptyLines: Boolean) : DsvFormatEvent()
    class RecordSeparatorChanged(val recordSeparator: String?) : DsvFormatEvent()
    class CommentMarkerChanged(val commentMarker: Char?) : DsvFormatEvent()
    class SkipHeaderRecordChanged(val skipHeaderRecord: Boolean) : DsvFormatEvent()
    class HeaderColumnNameChanged(val position: Int, val columnName: String) : DsvFormatEvent()
    class IgnoreHeaderCaseChanged(val ignoreHeaderCase: Boolean) : DsvFormatEvent()
    class AllowDuplicateHeaderNamesChanged(val allowDuplicateHeaderNames: Boolean) : DsvFormatEvent()
    class AllowMissingColumnNamesChanged(val allowMissingColumnNames: Boolean) : DsvFormatEvent()
    class HeaderCommentChanged(val position: Int, val headerComment: String) : DsvFormatEvent()
    class AutoFlushChanged(val autoFlush: Boolean) : DsvFormatEvent()
}