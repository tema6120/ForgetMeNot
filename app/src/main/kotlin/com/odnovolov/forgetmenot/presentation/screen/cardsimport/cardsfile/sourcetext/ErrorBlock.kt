package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.Parser.Error

data class ErrorBlock(
    val errorMessage: String,
    val lines: List<Int>
)

fun findErrorBlocks(text: String, errors: List<Error>): List<ErrorBlock> {
    val newLineCharLocations: MutableList<Int> = ArrayList()
    text.forEachIndexed { index, ch ->
        if (ch == '\n') newLineCharLocations.add(index)
    }
    return errors.map { error: Error ->
        val errorStart = error.errorRange.first
        val errorEnd = error.errorRange.last
        val errorLines: MutableList<Int> = ArrayList()
        if (newLineCharLocations.isEmpty()) {
            errorLines.add(0)
        } else {
            repeat(newLineCharLocations.size) { lineNumber: Int ->
                val lineStart =
                    if (lineNumber == 0) 0
                    else newLineCharLocations[lineNumber - 1] + 1
                val lineEnd = newLineCharLocations[lineNumber]
                when {
                    errorEnd < lineStart -> return@repeat
                    errorStart <= lineEnd -> errorLines.add(lineNumber)
                }
            }
            if (text.last() != '\n') {
                val lastLineStart = newLineCharLocations.last() + 1
                if (lastLineStart <= errorEnd) {
                    val lastLineNumber = newLineCharLocations.lastIndex + 1
                    errorLines.add(lastLineNumber)
                }
            }

        }
        ErrorBlock(error.message, errorLines)
    }
}