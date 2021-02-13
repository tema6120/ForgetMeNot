package com.odnovolov.forgetmenot.domain.interactor.fileimport

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader

class CsvParser(
    val csvFormat: CSVFormat
) : Parser() {
    private lateinit var text: String
    private val cardMarkups: MutableList<CardMarkup> = ArrayList()
    private val errors: MutableList<IntRange> = ArrayList()
    private val newLineCharLocations: MutableList<Int> = ArrayList()

    override fun parse(text: String): ParserResult {
        this.text = text
        cardMarkups.clear()
        errors.clear()
        newLineCharLocations.clear()
        text.forEachIndexed { index, ch ->
            if (ch == '\n') newLineCharLocations.add(index)
        }
        val reader = StringReader(text)
        val parser = csvFormat.parse(reader)
        var recordEnd = -1
        try {
            for (csvRecord: CSVRecord in parser) {
                val recordStart = csvRecord.characterPosition.toInt()
                val currentLineNumber = parser.currentLineNumber.toInt() - 1
                recordEnd =
                    if (currentLineNumber > newLineCharLocations.lastIndex) {
                        text.lastIndex
                    } else {
                        newLineCharLocations[currentLineNumber]
                    }
                if (csvRecord.size() < 2) {
                    errors.add(recordStart..recordEnd)
                    continue
                }
                val question = csvRecord[0].trim()
                val answer = csvRecord[1].trim()
                if (question.isEmpty() || answer.isEmpty()) {
                    errors.add(recordStart..recordEnd)
                    continue
                }

                val questionStart = text.indexOf(question, startIndex = recordStart)
                val questionEnd = questionStart + question.length - 1
                val questionRange = questionStart..questionEnd

                val answerStart = text.indexOf(answer, startIndex = questionEnd)
                val answerEnd = answerStart + answer.length - 1
                val answerRange = answerStart..answerEnd

                val cardPrototype = CardMarkup(questionRange, answerRange)
                cardMarkups.add(cardPrototype)
            }
        } catch (e: Exception) {
            val errorEnd = text.lastIndex
            val errorStart = minOf(recordEnd + 1, text.lastIndex)
            errors.add(errorStart..errorEnd)
        }
        return ParserResult(cardMarkups, errors)
    }
}