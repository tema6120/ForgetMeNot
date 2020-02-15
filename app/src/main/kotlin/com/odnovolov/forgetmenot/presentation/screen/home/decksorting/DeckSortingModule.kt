package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val deckSortingModule = module {
    scope<DeckSortingViewModel> {
        scoped { DeckSortingController(deckReviewPreference = get(), store = get()) }
        viewModel { DeckSortingViewModel(deckReviewPreference = get()) }
    }
}

const val DECK_SORTING_SCOPE_ID = "DECK_SORTING_SCOPE_ID"