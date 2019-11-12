package com.odnovolov.forgetmenot.screen.home.decksorting

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow

class DeckSortingViewModel {
    val deckSorting: Flow<DeckSorting> = database.deckSortingViewModelQueries
        .getDeckSorting()
        .asFlow()
        .mapToOne()
}