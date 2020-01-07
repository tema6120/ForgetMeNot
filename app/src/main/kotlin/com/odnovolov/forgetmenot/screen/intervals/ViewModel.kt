package com.odnovolov.forgetmenot.screen.intervals

import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntervalsViewModel {
    private val queries: IntervalsViewModelQueries = database.intervalsViewModelQueries

    val availableIntervalSchemes: Flow<List<Preset>> = queries
        .getAvailableIntervalSchemes(::Preset)
        .asFlow()
        .mapToList()

    val currentIntervalScheme: Flow<IntervalScheme?> = queries
        .getCurrentIntervalScheme()
        .asFlow()
        .mapToOneOrNull()

    val isSaveIntervalSchemeButtonEnabled: Flow<Boolean> = currentIntervalScheme
        .map { it != null && it.id != 0L && it.name.isEmpty() }

    val isNamePresetDialogVisible: Flow<Boolean> = queries
        .isNamePresetDialogVisible()
        .asFlow()
        .mapToOne()

    val dialogInputCheckResult: Flow<NameCheckResult> = queries
        .getDialogInputCheckResult()
        .asFlow()
        .mapToOne()
        .map { databaseValue: String -> nameCheckResultAdapter.decode(databaseValue) }

    val intervals: Flow<List<Interval>> = queries
        .getIntervals()
        .asFlow()
        .mapToList()

    val isRemoveIntervalButtonEnabled: Flow<Boolean> = intervals.map { it.size > 1 }
}