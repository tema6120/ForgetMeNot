package com.odnovolov.forgetmenot.domain.interactor.exercise

class TextInBracketsRemover {
    private val openingBrackets = listOf('(', '{', '[', '<')
    private val closingBrackets = listOf(')', '}', ']', '>')
    private val bracketsRanges: MutableList<IntRange> = ArrayList()
    private val indexedOpeningBrackets: MutableList<IndexedOpeningBracket> = ArrayList()
    private var inputString: String? = null

    fun process(inputString: String): String {
        this.inputString = inputString
        scan()
        return buildResult().also {
            resetState()
        }
    }

    private fun scan() {
        inputString!!.forEachIndexed { index, char ->
            if (char in openingBrackets) {
                indexedOpeningBrackets += IndexedOpeningBracket(char, index)
            } else if (char in closingBrackets) {
                findMatch(char)?.let { indexedOpeningBracket: IndexedOpeningBracket ->
                    bracketsRanges += indexedOpeningBracket.index..index
                    indexedOpeningBrackets.remove(indexedOpeningBracket)
                }
            }
        }
    }

    private fun findMatch(char: Char): IndexedOpeningBracket? {
        val match: Char = openingBrackets[closingBrackets.indexOf(char)]
        return indexedOpeningBrackets.findLast { it.bracket == match }
    }

    private fun buildResult(): String {
        val resultBuilder = StringBuilder()
        var lastChar = ' '
        inputString!!.forEachIndexed { index, char ->
            if (isCharInBrackets(index)) {
                if (lastChar != ' ') {
                    resultBuilder.append(' ')
                }
            } else {
                resultBuilder.append(char)
                lastChar = char
            }
        }
        return resultBuilder.toString()
    }

    private fun isCharInBrackets(index: Int): Boolean = bracketsRanges.any { index in it }

    private fun resetState() {
        inputString = null
        bracketsRanges.clear()
        indexedOpeningBrackets.clear()
    }

    private class IndexedOpeningBracket(val bracket: Char, val index: Int)
}