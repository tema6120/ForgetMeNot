package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import kotlinx.coroutines.flow.*

class SearchViewModel(
    screenState: SearchScreenState,
    private val globalState: GlobalState
) {
    private val searchDeck: Flow<Deck?> = flow {
        val deck: Deck? =
            if (DeckSetupDiScope.isOpenAsync()) {
                DeckSetupDiScope.get().screenState.relevantDeck
            } else {
                null
            }
        emit(deck)
    }
        .share()

    val searchDeckName: Flow<String?> = searchDeck.map { it?.name }

    val initialSearchText: String = screenState.searchText

    private val allCards: Flow<List<Pair<Card, Deck>>> = searchDeck.transform { searchDeck: Deck? ->
        val allCards: List<Pair<Card, Deck>> = if (searchDeck != null) {
            decomposeDeck(searchDeck)
        } else {
            globalState.decks.flatMap { deck: Deck -> decomposeDeck(deck) }
        }
        emit(allCards)
    }

    private fun decomposeDeck(deck: Deck): List<Pair<Card, Deck>> {
        return deck.cards.map { card: Card -> card to deck }
    }

    val cards: Flow<List<SearchCard>> = combine(
        screenState.flowOf(SearchScreenState::searchText),
        allCards
    ) { searchText: String, allCards: List<Pair<Card, Deck>> ->
        if (searchText.isEmpty())
            emptyList()
        else {
            allCards.filter { (card: Card, deck: Deck) ->
                card.question.contains(searchText, true) || card.answer.contains(searchText, true)
            }
                .map { (card: Card, deck: Deck) ->
                    SearchCard(
                        card,
                        deck,
                        questionMatchingRanges = findMatchingRange(card.question, searchText),
                        answerMatchingRanges = findMatchingRange(card.answer, searchText)
                    )
                }
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
}