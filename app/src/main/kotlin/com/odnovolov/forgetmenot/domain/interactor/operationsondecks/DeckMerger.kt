package com.odnovolov.forgetmenot.domain.interactor.operationsondecks

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckMerger.MergeData

class DeckMerger(
    private val globalState: GlobalState
) {
    fun merge(mergeData: MergeData) {
        val (decks: List<Deck>, destination: Deck) = mergeData
        val decksToMerge = decks.filter { deck: Deck -> deck.id != destination.id }
        val mergedCards: MutableList<Card> = destination.cards.toMutableList()
        for (deck in decksToMerge) {
            mergedCards.addAll(deck.cards)
        }
        val deckIdsToRemove = decksToMerge.map { deck: Deck -> deck.id }
        globalState.decks = globalState.decks
            .filter { deck: Deck -> deck.id !in deckIdsToRemove }
            .toCopyableList()
        destination.cards = mergedCards.toCopyableList()
    }

    data class MergeData (
        val decks: List<Deck>,
        val destination: Deck
    )
}

infix fun List<Deck>.into(destination: Deck) = MergeData(this, destination)