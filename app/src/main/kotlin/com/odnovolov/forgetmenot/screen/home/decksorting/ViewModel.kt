package com.odnovolov.forgetmenot.screen.home.decksorting

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import kotlinx.coroutines.flow.Flow

class DeckSortingViewModel {
    val deckSorting: Flow<DeckSorting> = database.deckSortingViewModelQueries
        .getDeckSorting(::DeckSorting)
        .asFlow()
        .mapToOne()
}