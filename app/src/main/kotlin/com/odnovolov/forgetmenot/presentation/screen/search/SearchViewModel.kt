package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.Searcher
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchViewModel(
    val initialSearchText: String,
    searcherState: Searcher.State
) {
    val searchDeckName: Flow<String?> = flow {
        val deckName: String? =
            if (DeckSetupDiScope.isOpenAsync()) {
                DeckSetupDiScope.getAsync().screenState.relevantDeck.name
            } else {
                null
            }
        emit(deckName)
    }

    val cards: Flow<List<SearchCard>> = searcherState.flowOf(Searcher.State::searchResult)

    val isSearching: Flow<Boolean> = searcherState.flowOf(Searcher.State::isSearching)
}