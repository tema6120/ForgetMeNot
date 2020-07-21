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

    private val allCards: Flow<List<Card>> = searchDeck.transform { searchDeck: Deck? ->
        val allCards: List<Card> = searchDeck?.cards ?: globalState.decks.flatMap(Deck::cards)
        emit(allCards)
    }

    val cards: Flow<List<SearchCard>> = combine(
        screenState.flowOf(SearchScreenState::searchText),
        allCards
    ) { searchText: String, allCards: List<Card> ->
        if (searchText.isEmpty())
            emptyList()
        else {
            allCards.filter { card: Card ->
                card.question.contains(searchText, true) || card.answer.contains(searchText, true)
            }
                .map { card: Card ->
                    SearchCard(
                        card,
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