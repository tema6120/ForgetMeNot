package com.odnovolov.forgetmenot.domain.feature.addnewdeck

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import java.io.InputStream
import java.lang.NullPointerException
import java.nio.charset.Charset

class Parser private constructor() {

    companion object {
        fun parse(inputStream: InputStream, charset: Charset): Deck {
            return Parser().parse(inputStream, charset)
        }

        private val CARD_BLOCK_SEPARATOR_REGEX = Regex("""\n(?=[[:blank:]]*Q:[[:blank:]]*\n)""")
        private val EMPTY_REGEX = Regex("""\s*""")
        private val CARD_REGEX = Regex(
            """\s*Q:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)\n[[:blank:]]*A:[[:blank:]]*\n(((?!^[[:blank:]]*[QA]:[[:blank:]]*${'$'})[\s\S])+)""",
            RegexOption.MULTILINE
        )
        private val CARD_CONTENT_REGEX = Regex("""[[:blank:]]*[\S]([\s\S]*[\S]|)""")
    }

    private fun parse(inputStream: InputStream, charset: Charset): Deck {
        val text = inputStream.bufferedReader().use {
            it.readText()
        }
        val cardBlocks: List<String> = text.split(CARD_BLOCK_SEPARATOR_REGEX).filter(::notEmpty)
        val cards: List<Card> = cardBlocks.map(::parseCardBlock)
        return DeckImpl(-1, cards)
    }

    private fun notEmpty(testedString: String): Boolean {
        return !testedString.matches(EMPTY_REGEX)
    }

    private fun parseCardBlock(cardBlock: String): Card {
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
        return CardImpl(-1, question, answer)
    }

    private fun trim(raw: String): String {
        val matchResult = CARD_CONTENT_REGEX.find(raw) ?: throw NullPointerException()
        return matchResult.value
    }

    private data class CardImpl(
        override val id: Long,
        override val question: String,
        override val answer: String
    ) : Card

    private data class DeckImpl(
        override val id: Long,
        override val cards: List<Card>
    ) : Deck
}