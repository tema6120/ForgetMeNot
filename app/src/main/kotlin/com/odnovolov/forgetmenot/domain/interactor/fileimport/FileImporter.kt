package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser.CardMarkup
import com.odnovolov.forgetmenot.domain.removeFirst
import java.nio.charset.Charset

class FileImporter(
    val state: State,
    private val globalState: GlobalState,
) {
    class State(
        files: List<CardsFile>,
        currentPosition: Int = 0
    ) : FlowMaker<State>() {
        var files: List<CardsFile> by flowMaker(files)
        var currentPosition: Int by flowMaker(currentPosition)

        companion object {
            fun fromFiles(files: List<ImportedFile>): State {
                val cardsFile: List<CardsFile> =
                    files.map { (fileName: String, content: ByteArray) ->
                        val charset: Charset = Charset.defaultCharset()
                        val parser: Parser = when (fileName.substringAfterLast('.', "")) {
                            "txt" -> FmnFormatParser()
                            //"csv" -> TODO()
                            else -> FmnFormatParser()
                        }
                        val text: String = content.toString(charset).let { contentString ->
                            if (parser is FmnFormatParser) {
                                contentString.removePrefix("\uFEFF").replace("\r", "")
                            } else {
                                contentString
                            }
                        }
                        val parseResult = parser.parse(text)
                        val errorLines: List<Int> = parseResult.errorLines
                        val cardPrototypes: List<CardPrototype> =
                            parseResult.cardMarkups.map { cardMarkup: CardMarkup ->
                                val question: String = text.substring(cardMarkup.questionRange)
                                val answer: String = text.substring(cardMarkup.answerRange)
                                CardPrototype(
                                    id = generateId(),
                                    question,
                                    answer,
                                    isSelected = true
                                )
                            }
                        val deckName = fileName.substringBeforeLast(".")
                        val deckWhereToAdd: AbstractDeck = NewDeck(deckName)
                        CardsFile(
                            sourceBytes = content,
                            charset,
                            text,
                            parser,
                            errorLines,
                            cardPrototypes,
                            deckWhereToAdd
                        )
                    }
                return State(cardsFile)
            }
        }
    }

    private val currentFile get() = with(state) { files[currentPosition] }

    fun setDeckWhereToAdd(deckWhereToAdd: AbstractDeck) {
        currentFile.deckWhereToAdd = deckWhereToAdd
    }

    fun setCharset(newCharset: Charset) {
        with(currentFile) {
            if (charset == newCharset) return
            val text: String = sourceBytes.toString(newCharset)
                .removePrefix("\uFEFF")
                .replace("\r", "")
            updateText(text)
            charset = newCharset
        }
    }

    fun updateText(text: String): Parser.ParserResult {
        val parseResult: Parser.ParserResult = currentFile.parser.parse(text)
        currentFile.text = text
        currentFile.errorLines = parseResult.errorLines
        val oldCardPrototypes: MutableList<CardPrototype> =
            currentFile.cardPrototypes.toMutableList()
        currentFile.cardPrototypes = parseResult.cardMarkups.map { cardMarkup: CardMarkup ->
            val question: String = text.substring(cardMarkup.questionRange)
            val answer: String = text.substring(cardMarkup.answerRange)
            oldCardPrototypes.removeFirst { cardPrototype: CardPrototype ->
                cardPrototype.question == question && cardPrototype.answer == answer
            } ?: CardPrototype(
                id = generateId(),
                question,
                answer,
                isSelected = true
            )
        }
        return parseResult
    }

    fun invertSelection(cardPrototypeId: Long) {
        with(currentFile) {
            cardPrototypes = cardPrototypes.map { cardPrototype: CardPrototype ->
                if (cardPrototypeId == cardPrototype.id)
                    cardPrototype.copy(isSelected = !cardPrototype.isSelected) else
                    cardPrototype
            }
        }
    }

    fun selectAll() {
        with(currentFile) {
            cardPrototypes = cardPrototypes.map { cardPrototype: CardPrototype ->
                if (cardPrototype.isSelected)
                    cardPrototype else
                    cardPrototype.copy(isSelected = true)
            }
        }
    }

    fun import(): List<Boolean> {
        val result: List<Boolean> = state.files.map { cardsFile: CardsFile ->
            val deckWhereToAdd = cardsFile.deckWhereToAdd
            if (deckWhereToAdd is NewDeck) {
                val nameCheckResult: NameCheckResult =
                    checkDeckName(deckWhereToAdd.deckName, globalState)
                if (nameCheckResult != NameCheckResult.Ok) {
                    return@map false
                }
            }
            val cardPrototypes: List<CardPrototype> = cardsFile.cardPrototypes
            if (cardPrototypes.isEmpty()) {
                return@map false
            }
            val newCards: CopyableList<Card> = cardPrototypes
                .filter { cardPrototype -> cardPrototype.isSelected }
                .map { cardPrototype -> cardPrototype.toCard() }
                .toCopyableList()
            when (deckWhereToAdd) {
                is NewDeck -> {
                    val deck = Deck(
                        id = generateId(),
                        name = deckWhereToAdd.deckName,
                        cards = newCards
                    )
                    globalState.decks = (globalState.decks + deck).toCopyableList()
                    return@map true
                }
                is ExistingDeck -> {
                    deckWhereToAdd.deck.cards =
                        (deckWhereToAdd.deck.cards + newCards).toCopyableList()
                    return@map true
                }
                else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
            }
        }
        state.files = state.files.filterIndexed { index, _ -> !result[index] }
        return result
    }
}