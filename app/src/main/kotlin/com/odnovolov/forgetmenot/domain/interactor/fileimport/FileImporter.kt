package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.Ok
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat.Companion.EXTENSION_CSV
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat.Companion.EXTENSION_TSV
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat.Companion.EXTENSION_TXT
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Failure.Cause
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Success
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser.CardMarkup
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser.Error
import com.odnovolov.forgetmenot.domain.removeFirst
import java.nio.charset.Charset

class FileImporter(
    val state: State,
    private val globalState: GlobalState,
    private val fileImportStorage: FileImportStorage
) {
    class State(
        files: List<CardsFile>,
        currentPosition: Int = 0,
        maxVisitedPosition: Int = 0
    ) : FlowMaker<State>() {
        var files: List<CardsFile> by flowMaker(files)
        var currentPosition: Int by flowMaker(currentPosition)
        var maxVisitedPosition: Int by flowMaker(maxVisitedPosition)

        companion object {
            fun fromFiles(files: List<ImportedFile>, fileImportStorage: FileImportStorage): State {
                val cardsFile: List<CardsFile> =
                    files.map { (fileName: String, content: ByteArray) ->
                        val charset: Charset = Charset.defaultCharset()
                        val extension = fileName.substringAfterLast('.', "")
                        val format = when (extension) {
                            EXTENSION_TXT -> fileImportStorage.lastUsedFormatForTxt
                            EXTENSION_CSV -> fileImportStorage.lastUsedFormatForCsv
                            EXTENSION_TSV -> fileImportStorage.lastUsedFormatForTsv
                            else -> FileFormat.FMN_FORMAT
                        }
                        val text: String = content.toString(charset)
                            .normalizeForParser(format.parser)
                        val parseResult = format.parser.parse(text)
                        val errors: List<Error> = parseResult.errors
                        val cardPrototypes: List<CardPrototype> =
                            parseResult.cardMarkups.map { cardMarkup: CardMarkup ->
                                val question: String = cardMarkup.questionText
                                val answer: String = cardMarkup.answerText
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
                            id = generateId(),
                            extension,
                            sourceBytes = content,
                            charset,
                            text,
                            format,
                            errors,
                            cardPrototypes,
                            deckWhereToAdd
                        )
                    }
                return State(cardsFile)
            }
        }
    }

    private val currentFile: CardsFile get() = with(state) { files[currentPosition] }

    fun setCurrentPosition(position: Int) {
        with(state) {
            if (position !in 0..files.lastIndex || position == currentPosition) return
            currentPosition = position
            if (position > maxVisitedPosition) {
                maxVisitedPosition = position
            }
        }
    }

    fun skip() {
        with(state) {
            if (files.size <= 1) return
            val position = currentPosition
            if (currentPosition == files.lastIndex) {
                currentPosition--
            }
            files = files.toMutableList().apply {
                removeAt(position)
            }
        }
    }

    fun setDeckWhereToAdd(deckWhereToAdd: AbstractDeck) {
        currentFile.deckWhereToAdd = deckWhereToAdd
    }

    fun setCharset(newCharset: Charset) {
        setCharsetForPosition(newCharset, state.currentPosition)
        for (position in state.files.indices) {
            if (position > state.maxVisitedPosition) {
                setCharsetForPosition(newCharset, position)
            }
        }
        fileImportStorage.lastUsedEncodingName = newCharset.name()
    }

    private fun setCharsetForPosition(newCharset: Charset, position: Int) {
        val file: CardsFile = state.files[position]
        if (file.charset == newCharset) return
        val reencodedText: String = file.sourceBytes.toString(newCharset)
            .normalizeForParser(file.format.parser)
        updateTextForPosition(reencodedText, position)
        file.charset = newCharset
    }

    fun setFormat(format: FileFormat) {
        setFormatForPosition(format, state.currentPosition)
        state.files.forEachIndexed { index, cardsFile ->
            if (index > state.maxVisitedPosition && cardsFile.extension == currentFile.extension) {
                setFormatForPosition(format, index)
            }
        }
        when (currentFile.extension) {
            EXTENSION_TXT -> fileImportStorage.lastUsedFormatForTxt = format
            EXTENSION_CSV -> fileImportStorage.lastUsedFormatForCsv = format
            EXTENSION_TSV -> fileImportStorage.lastUsedFormatForTsv = format
        }
    }

    private fun setFormatForPosition(format: FileFormat, position: Int) {
        val file: CardsFile = state.files[position]
        file.format = format
        updateTextForPosition(file.text, position)
    }

    fun updateText(text: String): Parser.ParserResult =
        updateTextForPosition(text, state.currentPosition)

    private fun updateTextForPosition(text: String, position: Int): Parser.ParserResult {
        val file: CardsFile = state.files[position]
        val parseResult: Parser.ParserResult = file.format.parser.parse(text)
        file.text = text
        file.errors = parseResult.errors
        val oldCardPrototypes: MutableList<CardPrototype> = file.cardPrototypes.toMutableList()
        file.cardPrototypes = parseResult.cardMarkups.map { cardMarkup: CardMarkup ->
            val question: String = cardMarkup.questionText
            val answer: String = cardMarkup.answerText
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

    fun useCurrentDeckForNextFiles() {
        val first = state.currentPosition + 1
        val last = state.files.lastIndex
        for (i in first..last) {
            val file = state.files[i]
            file.deckWhereToAdd = currentFile.deckWhereToAdd
        }
    }

    fun import(): ImportResult {
        for ((position: Int, cardsFile: CardsFile) in state.files.withIndex()) {
            val deckWhereToAdd = cardsFile.deckWhereToAdd
            if (deckWhereToAdd is NewDeck) {
                val nameCheckResult: NameCheckResult =
                    checkDeckName(deckWhereToAdd.deckName, globalState)
                if (nameCheckResult != Ok) {
                    return Failure(Cause.InvalidName(position))
                }
            }
        }
        val importedDecks: MutableList<Deck> = ArrayList(state.files.size)
        var numberOfImportedCards = 0
        for (cardsFile: CardsFile in state.files) {
            val newCards: CopyableList<Card> = cardsFile.cardPrototypes
                .filter { cardPrototype -> cardPrototype.isSelected }
                .map { cardPrototype -> cardPrototype.toCard() }
                .toCopyableList()
            if (newCards.isEmpty()) continue
            when (val deckWhereToAdd = cardsFile.deckWhereToAdd) {
                is NewDeck -> {
                    val deck = Deck(
                        id = generateId(),
                        name = deckWhereToAdd.deckName,
                        cards = newCards
                    )
                    globalState.decks = (globalState.decks + deck).toCopyableList()
                    importedDecks.add(deck)
                }
                is ExistingDeck -> {
                    deckWhereToAdd.deck.cards =
                        (deckWhereToAdd.deck.cards + newCards).toCopyableList()
                    importedDecks.add(deckWhereToAdd.deck)
                }
                else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
            }
            numberOfImportedCards += newCards.size
        }
        return if (numberOfImportedCards == 0) {
            Failure(Cause.NoCards)
        } else {
            Success(importedDecks, numberOfImportedCards)
        }
    }

    sealed class ImportResult {
        class Success(val decks: List<Deck>, val numberOfImportedCards: Int) : ImportResult()
        class Failure(val cause: Cause) : ImportResult() {
            sealed class Cause {
                class InvalidName(val position: Int) : Cause()
                object NoCards : Cause()
            }
        }
    }

    private companion object {
        val unwantedEOL by lazy { Regex("""\r\n?""") }

        fun String.normalizeForParser(parser: Parser): String {
            return when (parser) {
                is FmnFormatParser -> {
                    removePrefix("\uFEFF").replace(unwantedEOL, "\n")
                }
                else -> {
                    removePrefix("\uFEFF")
                }
            }
        }
    }
}