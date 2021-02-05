package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.CardPrototype
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.Parser
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.Parser.IllegalCardFormatException
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName

class FileImporter {
    class State(
        files: List<CardsFile>,
        currentPosition: Int = 0
    ) : FlowMaker<State>() {
        var files: List<CardsFile> by flowMaker(files)
        var currentPosition: Int by flowMaker(currentPosition)
    }

    constructor(
        importedFile: ImportedFile,
        globalState: GlobalState,
        fileImportSettings: FileImportSettings
    ) {
        val deckName = importedFile.fileName.substringBeforeLast(".")
        val whereToAdd: AbstractDeck = NewDeck(deckName)
        val text = importedFile.content.toString(fileImportSettings.charset)
            .removePrefix("\uFEFF")
            .replace("\r", "")
        val cardsFile = CardsFile(whereToAdd, text, fileImportSettings.charset)
        this.state = State(listOf(cardsFile))
        this.globalState = globalState
        this.fileImportSettings = fileImportSettings
    }

    constructor(
        state: State,
        globalState: GlobalState,
        fileImportSettings: FileImportSettings
    ) {
        this.state = state
        this.globalState = globalState
        this.fileImportSettings = fileImportSettings
    }

    val state: State
    private val globalState: GlobalState
    private val fileImportSettings: FileImportSettings

    private val currentFile get() = with(state) { files[currentPosition] }

    fun updateText(newText: String) {
        currentFile.text = newText
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
            val cardPrototypes: List<CardPrototype> = try {
                Parser.parse(cardsFile.text)
            } catch (e: IllegalCardFormatException) {
                return@map false
            }
            if (cardPrototypes.isEmpty()) {
                return@map false
            }
            val newCards = cardPrototypes.map(CardPrototype::toCard).toCopyableList()
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
            }
            error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
        }
        state.files = state.files.filterIndexed { index, _ -> !result[index] }
        return result
    }

    fun setDeckWhereToAdd(deckWhereToAdd: AbstractDeck) {
        currentFile.deckWhereToAdd = deckWhereToAdd
    }
}