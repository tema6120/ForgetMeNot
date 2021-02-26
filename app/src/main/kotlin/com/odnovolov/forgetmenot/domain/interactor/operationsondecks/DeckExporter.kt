package com.odnovolov.forgetmenot.domain.interactor.operationsondecks

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CsvParser
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.IOException
import java.io.OutputStream

class DeckExporter {
    fun export(deck: Deck, fileFormat: FileFormat, outputStream: OutputStream): Boolean {
        val stringToWrite: String = when (fileFormat) {
            FileFormat.FMN_FORMAT -> deck.toFmnFormatString()
            else -> {
                val csvFormat: CSVFormat = (fileFormat.parser as CsvParser).csvFormat
                deck.toDsvFormatString(csvFormat)
            }
        }
        return try {
            outputStream.use { os ->
                os.bufferedWriter().use { bufferedWriter ->
                    bufferedWriter.write(stringToWrite)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun Deck.toFmnFormatString(): String {
        return cards.joinToString(separator = "\n\n\n\n") { card: Card ->
            "Q:\n${card.question}\nA:\n${card.answer}"
        }
    }

    private fun Deck.toDsvFormatString(csvFormat: CSVFormat): String {
        val sb = StringBuilder()
        val printer = CSVPrinter(sb, csvFormat)
        for (card: Card in cards) {
            printer.printRecord(card.question, card.answer)
        }
        return sb.toString()
    }
}