package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.checkDeckName
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.Failure
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.FailureCause.*
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.Success
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.State.Stage.*
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.Parser.IllegalCardFormatException
import java.io.InputStream

class DeckFromFileCreator(
    val state: State,
    private val globalState: GlobalState
) {
    class State : FlowMaker<State>() {
        var stage: Stage by flowMaker(Idle)
        var cardPrototypes: List<CardPrototype>? by flowMaker<List<CardPrototype>?>(null)

        enum class Stage {
            Idle,
            Parsing,
            WaitingForName
        }
    }

    fun loadFromFile(inputStream: InputStream, fileName: String): Result {
        state.stage = Parsing
        try {
            state.cardPrototypes = Parser.parse("inputStream")
        } catch (e: IllegalCardFormatException) {
            return Failure(ParsingError(e))
        } finally {
            state.stage = Idle
        }
        val nameWithoutExtension = fileName.substringBeforeLast(".")
        return tryToAdd(nameWithoutExtension)
    }

    fun proposeDeckName(deckName: String): Result {
        check(state.stage != WaitingForName) { "Deck name is not expected now" }
        return tryToAdd(deckName)
    }

    fun cancel() {
        state.cardPrototypes = null
        state.stage = Idle
    }

    private fun tryToAdd(deckName: String): Result {
        return when (checkDeckName(deckName, globalState)) {
            Ok -> {
                val deck = addDeck(deckName)
                state.stage = Idle
                state.cardPrototypes = null
                Success(deck)
            }
            Empty -> {
                state.stage = WaitingForName
                Failure(DeckNameIsEmpty)
            }
            Occupied -> {
                state.stage = WaitingForName
                Failure(DeckNameIsOccupied(deckName))
            }
        }
    }

    private fun addDeck(deckName: String): Deck {
        val cards: CopyableList<Card> = state.cardPrototypes!!.map {
            Card(
                id = generateId(),
                question = it.question,
                answer = it.answer
            )
        }.toCopyableList()
        val deck = Deck(
            id = generateId(),
            name = deckName,
            cards = cards
        )
        globalState.decks = (globalState.decks + deck).toCopyableList()
        return deck
    }

    sealed class Result {
        data class Success(val deck: Deck) : Result()
        data class Failure(val failureCause: FailureCause) : Result()

        sealed class FailureCause {
            data class ParsingError(val exception: IllegalCardFormatException) : FailureCause()
            data class DeckNameIsOccupied(val occupiedName: String) : FailureCause()
            object DeckNameIsEmpty : FailureCause()
        }
    }
}