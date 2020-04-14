package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import kotlinx.coroutines.flow.Flow

class DeckSortingViewModel(deckReviewPreference: DeckReviewPreference) {
    val deckSorting: Flow<DeckSorting> =
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting)
}