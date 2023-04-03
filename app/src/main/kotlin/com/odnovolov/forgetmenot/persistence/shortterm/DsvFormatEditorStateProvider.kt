package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.*
import com.odnovolov.forgetmenot.persistence.shortterm.DsvFormatEditorStateProvider.SerializableState
import com.odnovolov.forgetmenot.persistence.shortterm.SerializableFileFormat.ExistingFileFormat
import com.odnovolov.forgetmenot.persistence.shortterm.SerializableFileFormat.NewFileFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.QuoteMode

class DsvFormatEditorStateProvider(
    json: Json,
    database: Database,
    private val cardsImportStorage: CardsImportStorage,
    override val key: String = DsvFormatEditor.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DsvFormatEditor.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val serializableFileFormat: SerializableFileFormat,
        val formatName: String,
        val errorMessage: String?,
        val delimiter: Char?,
        val trailingDelimiter: Boolean,
        val quoteCharacter: Char?,
        val quoteMode: QuoteMode?,
        val escapeCharacter: Char?,
        val nullString: String?,
        val ignoreSurroundingSpaces: Boolean,
        val trim: Boolean,
        val ignoreEmptyLines: Boolean,
        val recordSeparator: String?,
        val commentMarker: Char?,
        val skipHeaderRecord: Boolean,
        val header: Array<String?>?,
        val ignoreHeaderCase: Boolean,
        val allowDuplicateHeaderNames: Boolean,
        val allowMissingColumnNames: Boolean,
        val headerComments: Array<String?>?,
        val autoFlush: Boolean
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DsvFormatEditor.State): SerializableState {
        val sourceCSVFormat: CSVFormat = (state.editingFileFormat.parser as CsvParser).csvFormat
        val serializableFileFormat: SerializableFileFormat =
            when {
                state.editingFileFormat.isPredefined -> {
                    ExistingFileFormat(state.editingFileFormat.id)
                }
                cardsImportStorage.customFileFormats.any { it.id == state.editingFileFormat.id } -> {
                    ExistingFileFormat(state.editingFileFormat.id)
                }
                else -> {
                    val fileFormatWhereParserComeFrom: CardsFileFormat =
                        CardsFileFormat.predefinedFormats.find { fileFormat: CardsFileFormat ->
                            val csvParser = (fileFormat.parser as? CsvParser) ?: return@find false
                            csvParser.csvFormat == sourceCSVFormat
                        } ?: cardsImportStorage.customFileFormats.find {
                            val csvParser = it.parser as CsvParser
                            csvParser.csvFormat == sourceCSVFormat
                        } ?: CardsFileFormat.CSV_DEFAULT
                    NewFileFormat(
                        state.editingFileFormat.id,
                        state.editingFileFormat.name,
                        state.editingFileFormat.extension,
                        fileFormatWhereParserComeFrom.id
                    )
                }
            }
        return SerializableState(
            serializableFileFormat,
            state.formatName,
            state.errorMessage,
            state.delimiter,
            state.trailingDelimiter,
            state.quoteCharacter,
            state.quoteMode,
            state.escapeCharacter,
            state.nullString,
            state.ignoreSurroundingSpaces,
            state.trim,
            state.ignoreEmptyLines,
            state.recordSeparator,
            state.commentMarker,
            state.skipHeaderRecord,
            state.header,
            state.ignoreHeaderCase,
            state.allowDuplicateHeaderNames,
            state.allowMissingColumnNames,
            state.headerComments,
            state.autoFlush
        )
    }

    override fun toOriginal(serializableState: SerializableState): DsvFormatEditor.State {
        val editingFileFormat: CardsFileFormat =
            when (val serializableFileFormat = serializableState.serializableFileFormat) {
                is ExistingFileFormat -> {
                    CardsFileFormat.predefinedFormats.find { fileFormat: CardsFileFormat ->
                        fileFormat.id == serializableFileFormat.fileFormatId
                    } ?: cardsImportStorage.customFileFormats.find { fileFormat: CardsFileFormat ->
                        fileFormat.id == serializableFileFormat.fileFormatId
                    } ?: error(
                        "There is no file format either in the database or among the " +
                                "predefined file formats by id ${serializableFileFormat.fileFormatId}"
                    )
                }
                is NewFileFormat -> {
                    val fileFormatIdWhereParserComeFrom: CardsFileFormat =
                        CardsFileFormat.predefinedFormats.find { fileFormat: CardsFileFormat ->
                            fileFormat.id == serializableFileFormat.fileFormatIdWhereParserComeFrom
                        } ?: cardsImportStorage.customFileFormats.find { fileFormat: CardsFileFormat ->
                            fileFormat.id == serializableFileFormat.fileFormatIdWhereParserComeFrom
                        } ?: error(
                            "There is no file format either in the database or among the " +
                                    "predefined file formats by id " +
                                    serializableFileFormat.fileFormatIdWhereParserComeFrom
                        )
                    CardsFileFormat(
                        serializableFileFormat.fileFormatId,
                        serializableFileFormat.name,
                        serializableFileFormat.extension,
                        fileFormatIdWhereParserComeFrom.parser,
                        isPredefined = false
                    )
                }
            }
        return DsvFormatEditor.State(
            editingFileFormat,
            serializableState.formatName,
            serializableState.errorMessage,
            serializableState.delimiter,
            serializableState.trailingDelimiter,
            serializableState.quoteCharacter,
            serializableState.quoteMode,
            serializableState.escapeCharacter,
            serializableState.nullString,
            serializableState.ignoreSurroundingSpaces,
            serializableState.trim,
            serializableState.ignoreEmptyLines,
            serializableState.recordSeparator,
            serializableState.commentMarker,
            serializableState.skipHeaderRecord,
            serializableState.header,
            serializableState.ignoreHeaderCase,
            serializableState.allowDuplicateHeaderNames,
            serializableState.allowMissingColumnNames,
            serializableState.headerComments,
            serializableState.autoFlush
        )
    }
}

@Serializable
sealed class SerializableFileFormat {
    @Serializable
    class ExistingFileFormat(val fileFormatId: Long) : SerializableFileFormat()

    @Serializable
    class NewFileFormat(
        val fileFormatId: Long,
        val name: String,
        var extension: String,
        var fileFormatIdWhereParserComeFrom: Long
    ) : SerializableFileFormat()
}