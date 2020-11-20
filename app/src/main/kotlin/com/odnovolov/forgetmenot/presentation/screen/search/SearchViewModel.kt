package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class SearchViewModel(
    val initialSearchText: String,
    searcherState: CardsSearcher.State
) {
    val searchDeckName: Flow<String?> = flow {
        val deckName: String? = DeckSetupDiScope.getAsync()?.let { diScope: DeckSetupDiScope ->
            diScope.screenState.relevantDeck.name
        }
        emit(deckName)
    }

    val foundCards: Flow<List<SearchCard>> = searcherState.flowOf(CardsSearcher.State::searchResult)

    val isSearching: Flow<Boolean> = searcherState.flowOf(CardsSearcher.State::isSearching)

    val cardsNotFound: Flow<Boolean> = combine(
        searcherState.flowOf(CardsSearcher.State::searchText),
        isSearching,
        foundCards
    ) { searchText: String, isSearching: Boolean, foundCards: List<SearchCard> ->
        searchText.isNotEmpty() && !isSearching && foundCards.isEmpty()
    }
        .distinctUntilChanged()
}