package com.odnovolov.forgetmenot.screen.repetition.service

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow

class RepetitionServiceModel {
    private val queries: RepetitionServiceModelQueries = database.repetitionServiceModelQueries

    val question: Flow<String> = queries.getQuestion().asFlow().mapToOne()
    val isPlaying: Flow<Boolean> = queries.isPlaying().asFlow().mapToOne()
}