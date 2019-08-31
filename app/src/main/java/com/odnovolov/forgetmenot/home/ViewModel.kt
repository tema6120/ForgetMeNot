package com.odnovolov.forgetmenot.home

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToList
import kotlinx.coroutines.flow.Flow

class HomeViewModel {
    val decksPreview: Flow<List<DeckPreview>> = database.homeViewModelQueries
        .deckPreview()
        .asFlow()
        .mapToList()
}