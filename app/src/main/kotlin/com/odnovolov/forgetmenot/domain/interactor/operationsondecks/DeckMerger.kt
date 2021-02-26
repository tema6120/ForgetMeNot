package com.odnovolov.forgetmenot.domain.interactor.operationsondecks

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckMerger.MergeData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class DeckMerger(
    private val globalState: GlobalState,
    coroutineContext: CoroutineContext,
    private val timeToKeepBackup: Long = 10_000L
) {
    data class MergeData (
        val decks: List<Deck>,
        val destination: Deck
    )

    private val coroutineScope = CoroutineScope(coroutineContext)
    private var backupKeeping: Job? = null
    private var restore: (() -> Unit)? = null

    fun merge(mergeData: MergeData): Int {
        backupKeeping?.cancel()
        val (decks: List<Deck>, destination: Deck) = mergeData
        val decksToMerge = decks.filter { deck: Deck -> deck.id != destination.id }
        if (decksToMerge.isEmpty()) return 0
        val mergedCards: MutableList<Card> = destination.cards.toMutableList()
        for (deck in decksToMerge) {
            mergedCards.addAll(deck.cards)
        }
        val decksBackup = globalState.decks
        val cardsBackup = destination.cards
        restore = {
            destination.cards = cardsBackup
            globalState.decks = decksBackup
        }
        val deckIdsToRemove = decksToMerge.map { deck: Deck -> deck.id }
        globalState.decks = globalState.decks
            .filter { deck: Deck -> deck.id !in deckIdsToRemove }
            .toCopyableList()
        destination.cards = mergedCards.toCopyableList()
        coroutineScope.launch {
            delay(timeToKeepBackup)
            restore = null
        }
        return decksToMerge.size
    }

    fun cancel() {
        backupKeeping?.cancel()
        restore?.invoke()
        backupKeeping = null
        restore = null
    }

    fun dispose() {
        coroutineScope.cancel()
    }
}

infix fun List<Deck>.into(destination: Deck) = MergeData(this, destination)