package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import org.apache.commons.csv.CSVFormat

class FileFormat(
    override val id: Long,
    name: String,
    extension: String,
    parser: Parser,
    isPredefined: Boolean
) : FlowMakerWithRegistry<FileFormat>() {
    var name: String by flowMaker(name)
    var extension: String by flowMaker(extension)
    var parser: Parser by flowMaker(parser)
    val isPredefined: Boolean by flowMaker(isPredefined)

    override fun copy() = FileFormat(
        id,
        name,
        extension,
        parser,
        isPredefined
    )

    companion object {
        const val EXTENSION_TXT = "txt"
        const val EXTENSION_CSV = "csv"
        const val EXTENSION_TSV = "tsv"

        val predefinedFormats: List<FileFormat> by lazy {
            listOf(
                FMN_FORMAT,
                CSV_DEFAULT,
                CSV_EXCEL,
                CSV_EXCEL_SEMICOLON,
                CSV_MYSQL,
                CSV_RFC4180,
                CSV_TDF
            )
        }

        val FMN_FORMAT by lazy {
            FileFormat(
                id = 0,
                name = "Q:A:",
                extension = EXTENSION_TXT,
                parser = FmnFormatParser(),
                isPredefined = true
            )
        }

        val CSV_DEFAULT by lazy {
            FileFormat(
                id = 1,
                name = "CSV | Default",
                extension = EXTENSION_CSV,
                parser = CsvParser(CSVFormat.DEFAULT),
                isPredefined = true,
            )
        }

        val CSV_EXCEL by lazy {
            FileFormat(
                id = 2,
                name = "CSV | Excel",
                extension = EXTENSION_CSV,
                parser = CsvParser(CSVFormat.EXCEL),
                isPredefined = true
            )
        }

        val CSV_EXCEL_SEMICOLON by lazy {
            FileFormat(
                id = 3,
                name = "CSV | Excel (semicolon)",
                extension = EXTENSION_CSV,
                parser = CsvParser(CSVFormat.EXCEL.withDelimiter(';')),
                isPredefined = true
            )
        }

        val CSV_MYSQL by lazy {
            FileFormat(
                id = 4,
                name = "CSV | MySQL",
                extension = EXTENSION_CSV,
                parser = CsvParser(CSVFormat.MYSQL),
                isPredefined = true
            )
        }

        val CSV_RFC4180 by lazy {
            FileFormat(
                id = 5,
                name = "CSV | RFC-4180",
                extension = EXTENSION_CSV,
                parser = CsvParser(CSVFormat.RFC4180),
                isPredefined = true
            )
        }

        val CSV_TDF by lazy {
            FileFormat(
                id = 6,
                name = "Tab text",
                extension = EXTENSION_TSV,
                parser = CsvParser(CSVFormat.TDF),
                isPredefined = true
            )
        }
    }
}