package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.fileimport.DsvFormatEditor
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.apache.commons.csv.QuoteMode

class DsvFormatViewModel(
    private val dsvFormatEditorState: DsvFormatEditor.State,
    screenState: DsvFormatScreenState,
    private val fileImportStorage: FileImportStorage
) {
    val isReadOnly: Boolean get() = dsvFormatEditorState.editingFileFormat.isPredefined
    val formatName: String get() = dsvFormatEditorState.formatName

    val formatNameCheckResult: Flow<NameCheckResult> =
        fileImportStorage.flowOf(FileImportStorage::customFileFormats)
            .map { customFileFormats: CopyableCollection<FileFormat> ->
                customFileFormats
                    .filter { fileFormat: FileFormat ->
                        fileFormat.id != dsvFormatEditorState.editingFileFormat.id
                    }
                    .map { fileFormat: FileFormat -> fileFormat.name}
            }
            .combine(
                dsvFormatEditorState.flowOf(DsvFormatEditor.State::formatName)
            ) { occupiedNames: List<String>, testedName: String ->
                when {
                    testedName.isEmpty() -> NameCheckResult.Empty
                    testedName in occupiedNames -> NameCheckResult.Occupied
                    else -> NameCheckResult.Ok
                }
            }

    val isTipVisible: Flow<Boolean> = screenState.flowOf(DsvFormatScreenState::isTipVisible)
    val errorMessage: Flow<String?> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::errorMessage)
    val delimiter: Char? get() = dsvFormatEditorState.delimiter
    val trailingDelimiter: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::trailingDelimiter)
    val quoteCharacter: Flow<Char?> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::quoteCharacter)
    val quoteMode: Flow<QuoteMode?> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::quoteMode)
    val escapeCharacter: Flow<Char?> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::escapeCharacter)
    val nullString: Flow<String?> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::nullString)
    val ignoreSurroundingSpaces: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::ignoreSurroundingSpaces)
    val trim: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::trim)
    val ignoreEmptyLines: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::ignoreEmptyLines)
    val recordSeparator: String? get() = dsvFormatEditorState.recordSeparator
    val commentMarker: Flow<Char?> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::commentMarker)
    val skipHeaderRecord: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::skipHeaderRecord)
    val header: Array<String?>? get() = dsvFormatEditorState.header
    val headerColumnCount: Flow<Int> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::header).map { it?.size ?: 0 }
    val ignoreHeaderCase: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::ignoreHeaderCase)
    val allowDuplicateHeaderNames: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::allowDuplicateHeaderNames)
    val allowMissingColumnNames: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::allowMissingColumnNames)
    val headerComments: Array<String?>? get() = dsvFormatEditorState.headerComments
    val headerCommentsCount: Flow<Int> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::headerComments).map { it?.size ?: 0 }
    val autoFlush: Flow<Boolean> = dsvFormatEditorState.flowOf(DsvFormatEditor.State::autoFlush)
}