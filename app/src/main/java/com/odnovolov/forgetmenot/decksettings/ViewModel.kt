package com.odnovolov.forgetmenot.decksettings

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.database.mapToOneOrDefault
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
        .mapToOneOrDefault(DEFAULT_RANDOM_ORDER)

    val pronunciationName: Flow<String> = queries
        .getPronunciationName()
        .asFlow()
        .mapToOneOrDefault(DEFAULT_PRONUNCIATION_NAME)
}

const val DEFAULT_RANDOM_ORDER = true
const val DEFAULT_PRONUNCIATION_NAME = "Default"