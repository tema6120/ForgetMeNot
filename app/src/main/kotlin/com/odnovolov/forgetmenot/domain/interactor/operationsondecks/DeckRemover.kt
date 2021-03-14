package com.odnovolov.forgetmenot.domain.interactor.operationsondecks

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.recheckDeckIdsInDeckLists

class DeckRemover(
    private val globalState: GlobalState
) {
    private var restore: (() -> Unit)? =null

    fun removeDeck(deckId: Long): Int = removeDecks(listOf(deckId))

    fun removeDecks(deckIds: List<Long>): Int {
        val (removingDecks: List<Deck>, remainingDecks: List<Deck>) =
            globalState.decks.partition { deck: Deck -> deck.id in deckIds }
        val deckListsBackup: Map<DeckList, Set<Long>> =
            globalState.deckLists.associateWith { deckList: DeckList -> deckList.deckIds }
        restore = {
            globalState.decks = (globalState.decks + removingDecks).toCopyableList()
            deckListsBackup.forEach { (deckList: DeckList, deckIds: Set<Long>) ->
                deckList.deckIds = deckIds
            }
            recheckDeckIdsInDeckLists(globalState)
        }
        globalState.decks = remainingDecks.toCopyableList()
        recheckDeckIdsInDeckLists(globalState)
        return removingDecks.size
    }

    fun cancelRemoving() {
        restore?.invoke()
        restore = null
    }
}