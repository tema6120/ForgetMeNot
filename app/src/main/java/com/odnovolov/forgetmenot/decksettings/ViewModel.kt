package com.odnovolov.forgetmenot.decksettings

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckSettingsViewModel {
    private val queries: DeckSettingsViewModelQueries = database.deckSettingsViewModelQueries

    val deckName: Flow<String> = queries
        .getDeckName()
        .asFlow()
        .mapToOne()

    val exercisePreferenceIdAndName: Flow<ExercisePreferenceIdAndName> = queries
        .exercisePreferenceIdAndName()
        .asFlow()
        .mapToOne()

    val isSaveExercisePreferenceButtonEnabled: Flow<Boolean> = exercisePreferenceIdAndName
        .map { it.id != 0L && it.name.isEmpty() }

    val randomOrder: Flow<Boolean> = queries
        .getRandomOrder()
        .asFlow()
        .mapToOne()

    val pronunciationIdAndName: Flow<PronunciationIdAndName> = queries
        .pronunciationIdAndName()
        .asFlow()
        .mapToOne()
}