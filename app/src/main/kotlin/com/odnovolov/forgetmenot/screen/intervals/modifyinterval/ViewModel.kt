package com.odnovolov.forgetmenot.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOneOrNull
import com.odnovolov.forgetmenot.screen.intervals.IntervalUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ModifyIntervalViewModel {
    private val queries: ModifyIntervalViewModelQueries = database.modifyIntervalViewModelQueries

    val intervalNumberText: String = queries.getIntervalNumber().executeAsOne().intervalNumber.let {
        it?.toString() ?: ""
    }

    val intervalUnit: IntervalUnit = queries.getIntervalUnit().executeAsOne().let {
        IntervalUnit.valueOf(it)
    }

    val isOkButtonEnabled: Flow<Boolean> = queries.getIntervalNumber()
        .asFlow()
        .mapToOneOrNull()
        .map { it?.intervalNumber }
        .map { it != null && it > 0}
}