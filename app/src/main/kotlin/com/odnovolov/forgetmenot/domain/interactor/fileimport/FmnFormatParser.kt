package com.odnovolov.forgetmenot.domain.interactor.fileimport

class FmnFormatParser : Parser() {
    private val cardBlockSeparatorRegex = Regex("""\n(?=[[:blank:]]*Q:[[:blank:]]*\n)""")
    private val cardRegex = Regex(
        """\s*Q:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)\n[[:blank:]]*A:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)""",
        RegexOption.MULTILINE
    )
    private val cardContentRegex = Regex("""[[:blank:]]*[\S]([\s\S]*[\S]|)""")

    private lateinit var text: String
    private lateinit var cardMarkups: MutableList<CardMarkup>
    private lateinit var errorLines: MutableList<Int>
    private lateinit var newLineCharLocations: List<Int>

    override fun parse(text: String): ParserResult {
        this.text = text
        cardMarkups = ArrayList()
        errorLines = ArrayList()
        newLineCharLocations = text.mapIndexedNotNull { index, ch ->
            if (ch == '\n') index else null
        }
        val matchResults: List<MatchResult> = cardBlockSeparatorRegex.findAll(text).toList()
        var cardBlockStartIndex = 0
        var cardBlockEndIndex =
            if (matchResults.isEmpty()) text.lastIndex
            else matchResults[0].range.first
        parseCardBlock(cardBlockStartIndex, cardBlockEndIndex)
        repeat(matchResults.count()) { i: Int ->
            cardBlockStartIndex = matchResults[i].range.last
            cardBlockEndIndex =
                if (i < matchResults.lastIndex)
                    matchResults[i + 1].range.first
                else
                    text.lastIndex
            parseCardBlock(cardBlockStartIndex, cardBlockEndIndex)
        }
        return ParserResult(cardMarkups, errorLines)
    }

    private fun parseCardBlock(cardBlockStartIndex: Int, cardBlockEndIndex: Int) {
        val cardBlock: String = text.substring(cardBlockStartIndex..cardBlockEndIndex)
        if (cardBlock.isBlank()) return
        if (!cardBlock.matches(cardRegex)) {
            commitErrorLines(cardBlockStartIndex, cardBlockEndIndex)
            return
        }
        val cardMatchResult = cardRegex.find(cardBlock)!!

        val questionGroup: MatchGroup = cardMatchResult.groups[1]!!
        val questionMatchResult = cardContentRegex.find(questionGroup.value)
        if (questionMatchResult == null) {
            commitErrorLines(cardBlockStartIndex, cardBlockEndIndex)
            return
        }

        val answerGroup: MatchGroup = cardMatchResult.groups[3]!!
        val answerMatchResult = cardContentRegex.find(answerGroup.value)
        if (answerMatchResult == null) {
            commitErrorLines(cardBlockStartIndex, cardBlockEndIndex)
            return
        }

        val questionStart: Int =
            cardBlockStartIndex + questionGroup.range.first + questionMatchResult.range.first
        val questionEnd: Int =
            cardBlockStartIndex + questionGroup.range.first + questionMatchResult.range.last
        val questionRange = questionStart..questionEnd

        val answerStart: Int =
            cardBlockStartIndex + answerGroup.range.first + answerMatchResult.range.first
        val answerEnd: Int =
            cardBlockStartIndex + answerGroup.range.first + answerMatchResult.range.last
        val answerRange = answerStart..answerEnd

        val cardPrototype = CardMarkup(questionRange, answerRange)
        cardMarkups.add(cardPrototype)
    }

    private fun commitErrorLines(errorStartIndex: Int, errorEndIndex: Int) {
        if (newLineCharLocations.isEmpty()) {
            errorLines.add(0)
        } else {
            repeat(newLineCharLocations.size) { lineNumber: Int ->
                val lineStartIndex =
                    if (lineNumber == 0) 0
                    else newLineCharLocations[lineNumber - 1] + 1
                val lineEndIndex = newLineCharLocations[lineNumber]
                when {
                    errorEndIndex < lineStartIndex -> return
                    errorStartIndex <= lineEndIndex -> errorLines.add(lineNumber)
                }
            }
            if (text.last() != '\n') {
                val lineNumber = newLineCharLocations.size
                val lineStartIndex = newLineCharLocations.last() + 1
                val lineEndIndex = text.lastIndex
                when {
                    errorEndIndex < lineStartIndex -> return
                    errorStartIndex <= lineEndIndex -> errorLines.add(lineNumber)
                }
            }
        }
    }
}