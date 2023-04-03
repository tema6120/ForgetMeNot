package com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CsvParser
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.stringArrayAdapter

class CardsFormatPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.fileFormatQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        val fileImportId: Long = change.propertyOwnerId
        val exists: Boolean = queries.exists(fileImportId).executeAsOne()
        if (!exists) return
        when (change.property) {
            CardsFileFormat::name -> {
                val name = change.newValue as String
                queries.updateName(name, fileImportId)
            }
            CardsFileFormat::extension -> {
                val extension = change.newValue as String
                queries.updateExtension(extension, fileImportId)
            }
            CardsFileFormat::parser -> {
                val parser = change.newValue as CsvParser
                val csvFormat = parser.csvFormat
                queries.updateCSVFormat(
                    csvFormat.delimiter.toString(),
                    csvFormat.trailingDelimiter,
                    csvFormat.quoteCharacter?.toString(),
                    csvFormat.quoteMode,
                    csvFormat.escapeCharacter?.toString(),
                    csvFormat.nullString,
                    csvFormat.ignoreSurroundingSpaces,
                    csvFormat.trim,
                    csvFormat.ignoreEmptyLines,
                    csvFormat.recordSeparator,
                    csvFormat.commentMarker?.toString(),
                    csvFormat.skipHeaderRecord,
                    csvFormat.header?.let(stringArrayAdapter::encode),
                    csvFormat.ignoreHeaderCase,
                    csvFormat.allowDuplicateHeaderNames,
                    csvFormat.allowMissingColumnNames,
                    csvFormat.headerComments?.let(stringArrayAdapter::encode),
                    csvFormat.autoFlush,
                    fileImportId
                )
            }
        }
    }
}