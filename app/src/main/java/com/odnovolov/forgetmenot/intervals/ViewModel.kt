package com.odnovolov.forgetmenot.intervals

import com.odnovolov.forgetmenot.common.database.*
import kotlinx.coroutines.flow.Flow

class IntervalsViewModel {
    private val queries: IntervalsViewModelQueries = database.intervalsViewModelQueries

    val intervalScheme: Flow<IntervalScheme?> = queries
        .getIntervalScheme()
        .asFlow()
        .mapToOneNotNull()

    val intervals: Flow<List<Interval>> = queries
        .getIntervals()
        .asFlow()
        .mapToList()
}