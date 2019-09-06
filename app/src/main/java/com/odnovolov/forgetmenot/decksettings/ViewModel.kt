package com.odnovolov.forgetmenot.decksettings

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow

class DeckSettingsViewModel {
    private val queries: DeckSettingsViewModelQueries = database.deckSettingsViewModelQueries

    val deckName: Flow<String> = queries
        .getDeckName()
        .asFlow()
        .mapToOne()

    val randomOrder: Flow<Boolean> = queries
        .getRandomOrder()
        .asFlow()
        .mapToOne()

    val pronunciationIdAndName: Flow<PronunciationIdAndName> = queries
        .pronunciationIdAndName()
        .asFlow()
        .mapToOne()
}