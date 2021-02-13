package com.odnovolov.forgetmenot.domain.interactor.fileimport

import org.apache.commons.csv.CSVFormat

data class FileFormat(
    val id: Long,
    val name: String,
    val extension: String,
    val parser: Parser,
    val isPredefined: Boolean
) {
    companion object {
        const val EXTENSION_TXT = "txt"
        const val EXTENSION_CSV = "csv"
        const val EXTENSION_TSV = "tsv"

        val predefinedFormats: List<FileFormat> by lazy {
            listOf(
                FMN_FORMAT,
                CSV_DEFAULT,
                CSV_EXCEL,
                CSV_MYSQL,
                CSV_RFC4180,
                CSV_TDF
            )
        }

        val FMN_FORMAT = FileFormat(
            id = 0,
            name = "Q:A:",
            extension = EXTENSION_TXT,
            parser = FmnFormatParser(),
            isPredefined = true
        )

        val CSV_DEFAULT = FileFormat(
            id = 1,
            name = "CSV | Default",
            extension = EXTENSION_CSV,
            parser = CsvParser(CSVFormat.DEFAULT),
            isPredefined = true
        )

        val CSV_EXCEL = FileFormat(
            id = 2,
            name = "CSV | Excel",
            extension = EXTENSION_CSV,
            parser = CsvParser(CSVFormat.EXCEL),
            isPredefined = true
        )

        val CSV_MYSQL = FileFormat(
            id = 3,
            name = "CSV | MySQL",
            extension = EXTENSION_CSV,
            parser = CsvParser(CSVFormat.MYSQL),
            isPredefined = true
        )

        val CSV_RFC4180 = FileFormat(
            id = 4,
            name = "CSV | RFC-4180",
            extension = EXTENSION_CSV,
            parser = CsvParser(CSVFormat.RFC4180),
            isPredefined = true
        )

        val CSV_TDF = FileFormat(
            id = 5,
            name = "TSV",
            extension = EXTENSION_TSV,
            parser = CsvParser(CSVFormat.TDF),
            isPredefined = true
        )
    }
}