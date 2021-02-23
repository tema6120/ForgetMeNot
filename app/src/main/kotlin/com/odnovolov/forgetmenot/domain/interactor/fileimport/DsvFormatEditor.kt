package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.plus
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.interactor.fileimport.DsvFormatEditor.SaveResult.Failure.Cause
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat.Companion.EXTENSION_CSV
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat.Companion.EXTENSION_TSV
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.QuoteMode

class DsvFormatEditor(
    val state: State,
    private val fileImportStorage: FileImportStorage
) {
    class State(
        sourceFileFormat: FileFormat,
        formatName: String,
        errorMessage: String?,
        delimiter: Char?,
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
        header: Array<String?>?,
        ignoreHeaderCase: Boolean,
        allowDuplicateHeaderNames: Boolean,
        allowMissingColumnNames: Boolean,
        headerComments: Array<String?>?,
        autoFlush: Boolean,
    ) : FlowMaker<State>() {
        val editingFileFormat: FileFormat by flowMaker(sourceFileFormat)
        var formatName: String by flowMaker(formatName)
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
        var header: Array<String?>? by flowMaker(header)
        var ignoreHeaderCase: Boolean by flowMaker(ignoreHeaderCase)
        var allowDuplicateHeaderNames: Boolean by flowMaker(allowDuplicateHeaderNames)
        var allowMissingColumnNames: Boolean by flowMaker(allowMissingColumnNames)
        var headerComments: Array<String?>? by flowMaker(headerComments)
        var autoFlush: Boolean by flowMaker(autoFlush)

        companion object {
            fun createFrom(fileFormat: FileFormat): State {
                val parser = fileFormat.parser
                check(parser is CsvParser) { "parser of FileFormat should be CsvParser" }
                val csvFormat: CSVFormat = parser.csvFormat
                return State(
                    fileFormat,
                    fileFormat.name,
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
            }
        }
    }

    private val readOnly get() = state.editingFileFormat.isPredefined

    fun setFormatName(formatName: String) {
        if (readOnly) return
        state.formatName = formatName
    }

    fun setDelimiter(delimiter: Char?) {
        if (readOnly) return
        state.delimiter = delimiter
        validate()
    }

    fun setTrailingDelimiter(trailingDelimiter: Boolean) {
        if (readOnly) return
        state.trailingDelimiter = trailingDelimiter
        validate()
    }

    fun setQuoteCharacter(quoteCharacter: Char?) {
        if (readOnly) return
        state.quoteCharacter = quoteCharacter
        validate()
    }

    fun setQuoteMode(quoteMode: QuoteMode?) {
        if (readOnly) return
        state.quoteMode = quoteMode
        validate()
    }

    fun setEscapeCharacter(escapeCharacter: Char?) {
        if (readOnly) return
        state.escapeCharacter = escapeCharacter
        validate()
    }

    fun setNullString(nullString: String?) {
        if (readOnly) return
        state.nullString = nullString
        validate()
    }

    fun setIgnoreSurroundingSpaces(ignoreSurroundingSpaces: Boolean) {
        if (readOnly) return
        state.ignoreSurroundingSpaces = ignoreSurroundingSpaces
        validate()
    }

    fun setTrim(trim: Boolean) {
        if (readOnly) return
        state.trim = trim
        validate()
    }

    fun setIgnoreEmptyLines(ignoreEmptyLines: Boolean) {
        if (readOnly) return
        state.ignoreEmptyLines = ignoreEmptyLines
        validate()
    }

    fun setRecordSeparator(recordSeparator: String?) {
        if (readOnly) return
        state.recordSeparator = recordSeparator
        validate()
    }

    fun setCommentMarker(commentMarker: Char?) {
        if (readOnly) return
        state.commentMarker = commentMarker
        validate()
    }

    fun setSkipHeaderRecord(skipHeaderRecord: Boolean) {
        if (readOnly) return
        state.skipHeaderRecord = skipHeaderRecord
        validate()
    }

    fun setHeaderColumnName(position: Int, columnName: String) {
        if (readOnly) return
        val currentHeader = state.header
        when {
            currentHeader == null -> {
                if (columnName.isEmpty()) return
                state.header = arrayOf(columnName)
            }
            position > currentHeader.lastIndex -> {
                state.header = Array(currentHeader.size + 1) { i: Int ->
                    if (i <= currentHeader.lastIndex) {
                        currentHeader[i]
                    } else {
                        columnName
                    }
                }
            }
            position == currentHeader.lastIndex && columnName.isEmpty() -> {
                state.header =
                    if (currentHeader.size <= 1) {
                        null
                    } else {
                        Array(currentHeader.size - 1) { i: Int ->
                            currentHeader[i]
                        }
                    }
            }
            else -> {
                state.header!![position] = columnName
            }
        }
        validate()
    }

    fun setIgnoreHeaderCase(ignoreHeaderCase: Boolean) {
        if (readOnly) return
        state.ignoreHeaderCase = ignoreHeaderCase
        validate()
    }

    fun setAllowDuplicateHeaderNames(allowDuplicateHeaderNames: Boolean) {
        if (readOnly) return
        state.allowDuplicateHeaderNames = allowDuplicateHeaderNames
        validate()
    }

    fun setAllowMissingColumnNames(allowMissingColumnNames: Boolean) {
        if (readOnly) return
        state.allowMissingColumnNames = allowMissingColumnNames
        validate()
    }

    fun setHeaderComment(position: Int, headerComment: String) {
        if (readOnly) return
        val currentHeaderComments = state.headerComments
        when {
            currentHeaderComments == null -> {
                if (headerComment.isEmpty()) return
                state.headerComments = arrayOf(headerComment)
            }
            position > currentHeaderComments.lastIndex -> {
                state.headerComments =
                    Array(currentHeaderComments.size + 1) { i: Int ->
                        if (i <= currentHeaderComments.lastIndex) {
                            currentHeaderComments[i]
                        } else {
                            headerComment
                        }
                    }
            }
            position == currentHeaderComments.lastIndex && headerComment.isEmpty() -> {
                state.headerComments =
                    if (currentHeaderComments.size <= 1) {
                        null
                    } else {
                        Array(currentHeaderComments.size - 1) { i: Int ->
                            currentHeaderComments[i]
                        }
                    }
            }
            else -> {
                state.headerComments!![position] = headerComment
            }
        }
        validate()
    }

    fun setAutoFlush(autoFlush: Boolean) {
        if (readOnly) return
        state.autoFlush = autoFlush
        validate()
    }

    private fun validate(): CSVFormat? {
        return with(state) {
            if (delimiter == null) {
                errorMessage = "The delimiter cannot be empty"
                null
            } else {
                try {
                    val format = CSVFormat.newFormat(delimiter!!)
                        .withTrailingDelimiter(trailingDelimiter)
                        .withQuote(quoteCharacter)
                        .withEscape(escapeCharacter)
                        .withQuoteMode(quoteMode)
                        .withNullString(nullString)
                        .withIgnoreSurroundingSpaces(ignoreSurroundingSpaces)
                        .withTrim(trim)
                        .withIgnoreEmptyLines(ignoreEmptyLines)
                        .withRecordSeparator(recordSeparator)
                        .withCommentMarker(commentMarker)
                        .withSkipHeaderRecord(skipHeaderRecord)
                        .let { format -> header?.let { format.withHeader(*it) } ?: format }
                        .withIgnoreHeaderCase(ignoreHeaderCase)
                        .withAllowDuplicateHeaderNames(allowDuplicateHeaderNames)
                        .withAllowMissingColumnNames(allowMissingColumnNames)
                        .let { format -> headerComments?.let { format.withHeaderComments(*it) } ?: format }
                        .withAutoFlush(autoFlush)
                    errorMessage = null
                    format
                } catch (e: Exception) {
                    errorMessage = e.message ?: e::class.java.simpleName
                    null
                }
            }
        }
    }

    fun hasChanges(): Boolean {
        val sourceCSVFormat = (state.editingFileFormat.parser as CsvParser).csvFormat
        return state.editingFileFormat.name != state.formatName
                || sourceCSVFormat.delimiter != state.delimiter
                || sourceCSVFormat.trailingDelimiter != state.trailingDelimiter
                || sourceCSVFormat.quoteCharacter != state.quoteCharacter
                || sourceCSVFormat.quoteMode != state.quoteMode
                || sourceCSVFormat.escapeCharacter != state.escapeCharacter
                || sourceCSVFormat.nullString != state.nullString
                || sourceCSVFormat.ignoreSurroundingSpaces != state.ignoreSurroundingSpaces
                || sourceCSVFormat.trim != state.trim
                || sourceCSVFormat.ignoreEmptyLines != state.ignoreEmptyLines
                || sourceCSVFormat.recordSeparator != state.recordSeparator
                || sourceCSVFormat.commentMarker != state.commentMarker
                || sourceCSVFormat.skipHeaderRecord != state.skipHeaderRecord
                || !contentEqualsTakingIntoAccountNullContent(sourceCSVFormat.header, state.header)
                || sourceCSVFormat.ignoreHeaderCase != state.ignoreHeaderCase
                || sourceCSVFormat.allowDuplicateHeaderNames != state.allowDuplicateHeaderNames
                || sourceCSVFormat.allowMissingColumnNames != state.allowMissingColumnNames
                || !contentEqualsTakingIntoAccountNullContent(sourceCSVFormat.headerComments, state.headerComments)
                || sourceCSVFormat.autoFlush != state.autoFlush
    }

    private fun contentEqualsTakingIntoAccountNullContent(
        first: Array<String?>?,
        second: Array<String?>?
    ): Boolean {
        val firstHasNullContent = first == null || first.all { it == null }
        val secondHasNullContent = second == null || second.all { it == null }
        return if (firstHasNullContent && secondHasNullContent)
            true
        else
            first contentEquals second
    }

    fun remove(fileImporter: FileImporter): Boolean {
        if (state.editingFileFormat.isPredefined) {
            return false
        } else {
            fileImporter.state.files.forEach { file: CardsFile ->
                if (file.format.id == state.editingFileFormat.id) {
                    file.format = FileFormat.CSV_DEFAULT
                }
            }
            fileImportStorage.customFileFormats = fileImportStorage.customFileFormats
                .filter { fileFormat: FileFormat -> fileFormat.id != state.editingFileFormat.id }
                .toCopyableList()
            return true
        }
    }

    fun save(): SaveResult {
        if (state.formatName.isEmpty()) return SaveResult.Failure(Cause.NameIsEmpty)
        val editingFileFormat = state.editingFileFormat
        val occupiedNames = fileImportStorage.customFileFormats
            .filter { fileFormat: FileFormat -> fileFormat.id != editingFileFormat.id }
            .map { fileFormat: FileFormat -> fileFormat.name }
        if (state.formatName in occupiedNames) return SaveResult.Failure(Cause.NameIsOccupied)
        val newCsvFormat: CSVFormat = validate() ?: return SaveResult.Failure(Cause.InvalidFormat)
        val newParser = CsvParser(newCsvFormat)
        editingFileFormat.name = state.formatName
        editingFileFormat.parser = newParser
        editingFileFormat.extension =
            if (newCsvFormat.delimiter == '\t')
                EXTENSION_TSV else
                EXTENSION_CSV
        if (editingFileFormat !in fileImportStorage.customFileFormats) {
            fileImportStorage.customFileFormats =
                fileImportStorage.customFileFormats + editingFileFormat
        }
        return SaveResult.Success
    }

    sealed class SaveResult {
        class Failure(val cause: Cause) : SaveResult() {
            enum class Cause {
                NameIsEmpty,
                NameIsOccupied,
                InvalidFormat
            }
        }

        object Success : SaveResult()
    }
}