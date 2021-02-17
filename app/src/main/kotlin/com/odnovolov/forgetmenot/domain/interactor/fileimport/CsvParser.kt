package com.odnovolov.forgetmenot.domain.interactor.fileimport

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader

class CsvParser(
    val csvFormat: CSVFormat
) : Parser() {
    private lateinit var text: String
    private val cardMarkups: MutableList<CardMarkup> = ArrayList()
    private val errors: MutableList<Error> = ArrayList()
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
                    val errorMessage = "Record has less than 2 values"
                    val errorRange = recordStart..recordEnd
                    val error = Error(errorMessage, errorRange)
                    errors.add(error)
                    continue
                }
                val questionText = csvRecord[0].trim()
                val answerText = csvRecord[1].trim()
                if (questionText.isEmpty() || answerText.isEmpty()) {
                    val errorMessage = when {
                        questionText.isEmpty() && answerText.isEmpty() ->
                            "Question and answer are blank"
                        questionText.isEmpty() -> "Question is blank"
                        answerText.isEmpty() -> "Answer is blank"
                        else -> "This can't be"
                    }
                    val errorRange = recordStart..recordEnd
                    val error = Error(errorMessage, errorRange)
                    errors.add(error)
                    continue
                }

                // naive way to find position
                val questionStart = text.indexOf(questionText, startIndex = recordStart)
                val questionEnd = questionStart + questionText.length - 1
                val questionRange =
                    if (questionStart != -1) {
                        questionStart..questionEnd
                    } else {
                        null
                    }

                val answerStart = text.indexOf(answerText, startIndex = questionEnd)
                val answerEnd = answerStart + answerText.length - 1
                val answerRange =
                    if (answerStart != -1) {
                        answerStart..answerEnd
                    } else {
                        null
                    }

                val cardPrototype = CardMarkup(
                    questionText,
                    questionRange,
                    answerText,
                    answerRange
                )
                cardMarkups.add(cardPrototype)
            }
        } catch (e: Exception) {
            val errorMessage: String = e.message ?: e::class.java.simpleName
            val errorEnd = text.lastIndex
            val errorStart = minOf(recordEnd + 1, text.lastIndex)
            val errorRange = errorStart..errorEnd
            val error = Error(errorMessage, errorRange)
            errors.add(error)
        }
        return ParserResult(cardMarkups, errors)
    }
}