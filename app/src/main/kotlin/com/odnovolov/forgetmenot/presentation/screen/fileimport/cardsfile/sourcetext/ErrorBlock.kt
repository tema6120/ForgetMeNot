package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext

data class ErrorBlock(
    val lines: List<Int>
)

fun findErrorBlocks(text: String, errorRanges: List<IntRange>): List<ErrorBlock> {
    val newLineCharLocations: MutableList<Int> = ArrayList()
    text.forEachIndexed { index, ch ->
        if (ch == '\n') newLineCharLocations.add(index)
    }
    return errorRanges.map { errorRange: IntRange ->
        val errorStart = errorRange.first
        val errorEnd = errorRange.last
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
        ErrorBlock(errorLines)
    }
}