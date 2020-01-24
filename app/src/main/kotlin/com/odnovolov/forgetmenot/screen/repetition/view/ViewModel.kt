package com.odnovolov.forgetmenot.screen.repetition.view

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToList
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow

class RepetitionViewModel {
    private val queries: RepetitionViewModelQueries = database.repetitionViewModelQueries

    val isPlaying: Flow<Boolean> = queries
        .isPlaying()
        .asFlow()
        .mapToOne()

    val repetitionCardItems: Flow<List<RepetitionCardItem>> = queries
        .repetitionCardItem()
        .asFlow()
        .mapToList()

    val currentRepetitionCardId: Flow<Long> = queries
        .getCurrentRepetitionCardId()
        .asFlow()
        .mapToOne()
}