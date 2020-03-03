package com.odnovolov.forgetmenot.domain.interactor.deckremover

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover.Event.DecksHasRemoved
import kotlinx.coroutines.flow.Flow

class DeckRemover(
    private val globalState: GlobalState
) {
    sealed class Event {
        class DecksHasRemoved(val count: Int) : Event()
    }

    private var deckBackup: List<Deck>? = null
    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()

    fun removeDeck(deckId: Long) = removeDecks(listOf(deckId))

    fun removeDecks(deckIds: List<Long>) {
        val (removingDecks: List<Deck>, remainingDecks: List<Deck>) =
            globalState.decks.partition { deck: Deck -> deck.id in deckIds }
        globalState.decks = remainingDecks.toCopyableList()
        deckBackup = removingDecks
        eventFlow.send(DecksHasRemoved(removingDecks.size))
    }

    fun restoreDecks() {
        if (deckBackup != null) {
            globalState.decks = (globalState.decks + deckBackup!!).toCopyableList()
            deckBackup = null
        }
    }
}