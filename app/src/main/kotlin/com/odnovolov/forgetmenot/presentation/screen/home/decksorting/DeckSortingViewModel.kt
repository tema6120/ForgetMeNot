package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import kotlinx.coroutines.flow.Flow

class DeckSortingViewModel(
    deckReviewPreference: DeckReviewPreference,
    val controller: DeckSortingController
) : ViewModel() {
    val deckSorting: Flow<DeckSorting> = deckReviewPreference
        .flowOf(DeckReviewPreference::deckSorting)
}