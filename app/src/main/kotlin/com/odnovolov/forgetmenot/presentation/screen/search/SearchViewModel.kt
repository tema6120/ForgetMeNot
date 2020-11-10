package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import kotlinx.coroutines.flow.Flow
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

    val cards: Flow<List<SearchCard>> = searcherState.flowOf(CardsSearcher.State::searchResult)

    val isSearching: Flow<Boolean> = searcherState.flowOf(CardsSearcher.State::isSearching)
}