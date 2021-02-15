package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.apache.commons.csv.QuoteMode

class DsvFormatViewModel(
    private val screenState: DsvFormatScreenState
) {
    val isReadOnly: Boolean get() = screenState.isReadOnly
    val formatName: Flow<String> = screenState.flowOf(DsvFormatScreenState::formatName)
    val isTipVisible: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::isTipVisible)
    val errorMessage: Flow<String?> = screenState.flowOf(DsvFormatScreenState::errorMessage)
    val delimiter: Char? get() = screenState.delimiter
    val trailingDelimiter: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::trailingDelimiter)
    val quoteCharacter: Flow<Char?> = screenState.flowOf(DsvFormatScreenState::quoteCharacter)
    val quoteMode: Flow<QuoteMode?> = screenState.flowOf(DsvFormatScreenState::quoteMode)
    val escapeCharacter: Flow<Char?> = screenState.flowOf(DsvFormatScreenState::escapeCharacter)
    val nullString: Flow<String?> = screenState.flowOf(DsvFormatScreenState::nullString)
    val ignoreSurroundingSpaces: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::ignoreSurroundingSpaces)
    val trim: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::trim)
    val ignoreEmptyLines: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::ignoreEmptyLines)
    val recordSeparator: String? get() = screenState.recordSeparator
    val commentMarker: Flow<Char?> = screenState.flowOf(DsvFormatScreenState::commentMarker)
    val skipHeaderRecord: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::skipHeaderRecord)
    val header: Array<String>? get() = screenState.header
    val headerColumnCount: Flow<Int> = screenState.flowOf(DsvFormatScreenState::header).map { it?.size ?: 0 }
    val ignoreHeaderCase: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::ignoreHeaderCase)
    val allowDuplicateHeaderNames: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::allowDuplicateHeaderNames)
    val allowMissingColumnNames: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::allowMissingColumnNames)
    val headerComments: Array<String>? get() = screenState.headerComments
    val headerCommentsCount: Flow<Int> = screenState.flowOf(DsvFormatScreenState::headerComments).map { it?.size ?: 0 }
    val autoFlush: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::autoFlush)
}