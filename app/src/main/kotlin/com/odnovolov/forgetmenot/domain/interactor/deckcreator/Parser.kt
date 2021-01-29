package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import java.io.InputStream

class Parser private constructor() {

    companion object {
        fun parse(inputStream: InputStream): List<CardPrototype> {
            return Parser().parse(inputStream)
        }

        private val CARD_BLOCK_SEPARATOR_REGEX = Regex("""\n(?=[[:blank:]]*Q:[[:blank:]]*\n)""")
        private val EMPTY_REGEX = Regex("""\s*""")
        private val CARD_REGEX = Regex(
            """\s*Q:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)\n[[:blank:]]*A:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)""",
            RegexOption.MULTILINE
        )
        private val CARD_CONTENT_REGEX = Regex("""[[:blank:]]*[\S]([\s\S]*[\S]|)""")
    }

    private fun parse(inputStream: InputStream): List<CardPrototype> {
        val text = inputStream.bufferedReader().use {
            it.readText()
        }
        return text
            .removePrefix("\uFEFF")
            .replace("\r", "")
            .split(CARD_BLOCK_SEPARATOR_REGEX)
            .filter(::notEmpty)
            .map(::parseCardBlock)
    }

    private fun notEmpty(testedString: String): Boolean {
        return !testedString.matches(EMPTY_REGEX)
    }

    private fun parseCardBlock(cardBlock: String): CardPrototype {
        if (!cardBlock.matches(CARD_REGEX)) {
            throw IllegalCardFormatException("wrong card format: $cardBlock")
        }
        val matchResult = CARD_REGEX.find(cardBlock)
        val questionRaw: String = matchResult!!.groups[1]?.value!!
        val answerRaw: String = matchResult.groups[3]?.value!!
        val question =
            try {
                trim(questionRaw)
            } catch (e: NullPointerException) {
                throw IllegalCardFormatException("card doesn't have question: $cardBlock")
            }
        val answer =
            try {
                trim(answerRaw)
            } catch (e: NullPointerException) {
                throw IllegalCardFormatException("card doesn't have answer: $cardBlock")
            }
        return CardPrototype(question, answer)
    }

    private fun trim(raw: String): String {
        val matchResult = CARD_CONTENT_REGEX.find(raw) ?: throw NullPointerException()
        return matchResult.value
    }

    class IllegalCardFormatException(override val message: String) : Exception(message)
}