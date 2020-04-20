package com.odnovolov.forgetmenot.domain.interactor.deckadder

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder.Event.*
import com.odnovolov.forgetmenot.domain.interactor.deckadder.Parser.IllegalCardFormatException
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

class DeckAdder(
    val state: State,
    private val globalState: GlobalState
) {
    class State : FlowableState<State>() {
        var stage: Stage by me(Stage.Idle)
        var cardPrototypes: List<CardPrototype>? by me<List<CardPrototype>?>(null)
    }

    sealed class Event {
        class ParsingFinishedWithError(val exception: IllegalCardFormatException) : Event()
        class DeckNameIsOccupied(val occupiedName: String) : Event()
        class DeckHasBeenAdded(val deck: Deck) : Event()
    }

    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()

    fun addFrom(inputStream: InputStream, deckName: String? = null) {
        val success = parse(inputStream)
        if (success) {
            val nameWithoutExtension = deckName?.substringBeforeLast(".")
            tryToAdd(nameWithoutExtension)
        }
    }

    fun proposeDeckName(deckName: String) {
        if (state.stage != Stage.WaitingForName) return
        tryToAdd(deckName)
    }

    fun cancel() {
        state.cardPrototypes = null
        state.stage = Stage.Idle
    }

    private fun parse(inputStream: InputStream): Boolean {
        state.stage = Stage.Parsing
        return try {
            state.cardPrototypes = Parser.parse(inputStream)
            state.stage = Stage.Idle
            true
        } catch (e: IllegalCardFormatException) {
            state.stage = Stage.Idle
            eventFlow.send(ParsingFinishedWithError(e))
            return false
        }
    }

    private fun tryToAdd(deckName: String?) {
        state.cardPrototypes ?: return
        when {
            deckName.isNullOrEmpty() -> {
                state.stage = Stage.WaitingForName
            }
            isDeckNameOccupied(deckName) -> {
                state.stage = Stage.WaitingForName
                eventFlow.send(DeckNameIsOccupied(deckName))
            }
            else -> {
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
                state.stage = Stage.Idle
                state.cardPrototypes = null
                eventFlow.send(DeckHasBeenAdded(deck))
            }
        }
    }

    private fun isDeckNameOccupied(testedName: String): Boolean {
        return globalState.decks.any { it.name == testedName }
    }
}