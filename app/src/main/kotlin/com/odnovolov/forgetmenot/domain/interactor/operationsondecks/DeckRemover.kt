package com.odnovolov.forgetmenot.domain.interactor.operationsondecks

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState

class DeckRemover(
    private val globalState: GlobalState
) {
    private var deckBackup: List<Deck>? = null

    fun removeDeck(deckId: Long): Int = removeDecks(listOf(deckId))

    fun removeDecks(deckIds: List<Long>): Int {
        val (removingDecks: List<Deck>, remainingDecks: List<Deck>) =
            globalState.decks.partition { deck: Deck -> deck.id in deckIds }
        globalState.decks = remainingDecks.toCopyableList()
        deckBackup = removingDecks
        return removingDecks.size
    }

    fun restoreDecks() {
        if (deckBackup != null) {
            globalState.decks = (globalState.decks + deckBackup!!).toCopyableList()
            deckBackup = null
        }
    }
}