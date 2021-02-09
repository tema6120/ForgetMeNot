package com.odnovolov.forgetmenot.domain.interactor.searcher

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher.SearchArea.AllDecks
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher.SearchArea.SpecificDeck
import kotlinx.coroutines.*

class CardsSearcher {
    constructor(globalState: GlobalState) {
        getAllCards = { globalState.decks.flatMap { deck: Deck -> decomposeDeck(deck) } }
        state = State(AllDecks)
    }

    constructor(deck: Deck) {
        getAllCards = { decomposeDeck(deck) }
        state = State(SpecificDeck(deck))
    }

    val state: State
    private val getAllCards: () -> List<Pair<Card, Deck>>
    private val coroutineScope = CoroutineScope(newSingleThreadContext("SearchThread"))
    private var searchJob: Job? = null

    private fun decomposeDeck(deck: Deck): List<Pair<Card, Deck>> {
        return deck.cards.map { card: Card -> card to deck }
    }

    fun search(text: String) {
        state.searchText = text
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            if (text.isEmpty()) {
                state.searchResult = emptyList()
                state.isSearching = false
                return@launch
            }
            state.isSearching = true
            val searchResult: MutableList<SearchCard> = ArrayList()
            getAllCards().forEach { (card: Card, deck: Deck) ->
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

    class State(
        searchArea: SearchArea
    ) : FlowMaker<State>() {
        val searchArea: SearchArea by flowMaker(searchArea)
        var searchText: String by flowMaker("")
        var isSearching: Boolean by flowMaker(false)
        var searchResult: List<SearchCard> by flowMaker(emptyList())
    }

    sealed class SearchArea {
        object AllDecks : SearchArea()
        class SpecificDeck(val deck: Deck) : SearchArea()
    }
}