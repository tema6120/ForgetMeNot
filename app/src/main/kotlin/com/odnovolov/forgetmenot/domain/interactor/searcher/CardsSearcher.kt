package com.odnovolov.forgetmenot.domain.interactor.searcher

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import kotlinx.coroutines.*

class CardsSearcher {
    constructor(globalState: GlobalState) {
        allCards = globalState.decks.flatMap { deck: Deck -> decomposeDeck(deck) }
    }

    constructor(deck: Deck) {
        allCards = decomposeDeck(deck)
    }

    val state = State()
    private val allCards: List<Pair<Card, Deck>>
    private val coroutineScope = CoroutineScope(newSingleThreadContext("SearchThread"))
    private var searchJob: Job? = null

    private fun decomposeDeck(deck: Deck): List<Pair<Card, Deck>> {
        return deck.cards.map { card: Card -> card to deck }
    }

    fun search(text: String) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            state.searchResult = emptyList()
            if (text.isEmpty()) {
                state.isSearching = false
                return@launch
            }
            state.isSearching = true
            val searchResult: MutableList<SearchCard> = ArrayList()
            allCards.forEach { (card: Card, deck: Deck) ->
                val questionMatchingRanges: List<IntRange> = findMatchingRange(card.question, text)
                val answerMatchingRanges: List<IntRange> = findMatchingRange(card.answer, text)
                if (questionMatchingRanges.isNotEmpty() || answerMatchingRanges.isNotEmpty()) {
                    val searchCard = SearchCard(
                        card,
                        deck,
                        questionMatchingRanges,
                        answerMatchingRanges
                    )
                    searchResult.add(searchCard)
                }
                if (!isActive) return@launch
            }
            state.searchResult = searchResult
            state.isSearching = false
        }
    }

    private fun findMatchingRange(source: String, search: String): List<IntRange> {
        var start = source.indexOf(search, ignoreCase = true)
        if (start < 0) return emptyList()
        val result = ArrayList<IntRange>()
        while (start >= 0) {
            val end = start + search.length
            result += start..end
            start = source.indexOf(search, startIndex = end, ignoreCase = true)
        }
        return result
    }

    fun dispose() {
        coroutineScope.cancel()
    }

    class State : FlowMaker<State>() {
        var isSearching: Boolean by flowMaker(false)
        var searchResult: List<SearchCard> by flowMaker(emptyList())
    }
}