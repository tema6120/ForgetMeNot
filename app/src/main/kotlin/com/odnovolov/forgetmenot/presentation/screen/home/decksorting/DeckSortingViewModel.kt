package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinComponent

class DeckSortingViewModel(
    deckReviewPreference: DeckReviewPreference
) : ViewModel(), KoinComponent {
    val deckSorting: Flow<DeckSorting> = deckReviewPreference
        .flowOf(DeckReviewPreference::deckSorting)

    override fun onCleared() {
        getKoin().getScope(DECK_SORTING_SCOPE_ID).close()
    }
}