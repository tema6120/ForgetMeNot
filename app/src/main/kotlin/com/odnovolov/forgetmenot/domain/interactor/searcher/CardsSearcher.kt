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
    private var timeToUpdateSearchResult: Long = 0
    private var isResearching = false

    private fun decomposeDeck(deck: Deck): List<Pair<Card, Deck>> {
        return deck.cards.map { card: Card -> card to deck }
    }

    fun search(text: String) {
        isResearching = false
        state.searchText = text
        search()
    }

    fun research() {
        isResearching = true
        search()
    }

    private fun search() {
        val searchText = state.searchText
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            if (searchText.isEmpty()) {
                state.searchResult = emptyList()
                state.isSearching = false
                return@launch
            }
            state.isSearching = true
            val searchResult: MutableList<FoundCard> = ArrayList()
            if (!isResearching) rememberTime()
            getAllCards().forEach { (card: Card, deck: Deck) ->
                if (card.question.contains(searchText, ignoreCase = true)
                    || card.answer.contains(searchText, ignoreCase = true)
                ) {
                    val searchCard = FoundCard(card, deck, searchText)
                    searchResult.add(searchCard)
                }
                if (!isActive) return@launch
                if (!isResearching && isTimeToUpdateSearchResult()) {
                    state.searchResult = searchResult.toList()
                    rememberTime()
                }
            }
            state.searchResult = searchResult
            state.isSearching = false
        }
    }

    private fun rememberTime() {
        timeToUpdateSearchResult =
            System.currentTimeMillis() + TIME_PERIOD_FOR_UPDATING_SEARCH_RESULTS
    }

    private fun isTimeToUpdateSearchResult(): Boolean {
        return System.currentTimeMillis() > timeToUpdateSearchResult
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
        var searchResult: List<FoundCard> by flowMaker(emptyList())
    }

    sealed class SearchArea {
        object AllDecks : SearchArea()
        class SpecificDeck(val deck: Deck) : SearchArea()
    }

    companion object {
        const val TIME_PERIOD_FOR_UPDATING_SEARCH_RESULTS = 100
    }
}