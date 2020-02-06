package com.odnovolov.forgetmenot.domain.interactor.adddeck

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.architecturecomponents.*
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck.Event.*
import com.odnovolov.forgetmenot.screen.home.adddeck.Parser.IllegalCardFormatException
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

class AddDeck(
    val state: State,
    private val globalState: GlobalState
) {
    class State : FlowableState<State>() {
        var stage: Stage by me(Stage.Idle)
        var cardPrototypes: List<CardPrototype>? by me(null)
    }

    sealed class Event {
        class ParsingFinishedWithError(val e: IllegalCardFormatException) : Event()
        class DeckNameIsOccupied(val occupiedName: String) : Event()
        class DeckHasAdded(val deck: Deck) : Event()
    }

    private val eventManager = EventFlow<Event>()
    val events: Flow<Event> = eventManager.get()

    fun addFrom(inputStream: InputStream, deckName: String? = null) {
        val success = parse(inputStream)
        if (success) {
            tryToAdd(deckName)
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
            eventManager.send(ParsingFinishedWithError(e))
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
                eventManager.send(DeckNameIsOccupied(deckName))
            }
            else -> {
                val cards: CopyableList<Card> = state.cardPrototypes!!.map {
                    Card(
                        id = SUID.id(),
                        question = it.question,
                        answer = it.answer
                    )
                }.toCopyableList()
                val deck = Deck(
                    id = SUID.id(),
                    name = deckName,
                    cards = cards
                )
                globalState.decks = (globalState.decks + deck).toCopyableList()
                state.stage = Stage.Idle
                state.cardPrototypes = null
                eventManager.send(DeckHasAdded(deck))
            }
        }
    }

    private fun isDeckNameOccupied(testedName: String): Boolean {
        return globalState.decks.any { it.name == testedName }
    }
}