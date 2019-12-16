package com.odnovolov.forgetmenot.screen.intervals

import com.odnovolov.forgetmenot.common.Interval
import com.odnovolov.forgetmenot.common.IntervalScheme
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.intervals.IntervalsViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IntervalsViewModel {
    private val queries: IntervalsViewModelQueries = database.intervalsViewModelQueries

    val availableIntervalSchemes: Flow<List<Preset>> = queries
        .getAvailableIntervalSchemes(mapper = { id: Long, name: String, isSelected: Long ->
            Preset(id, name, isSelected.asBoolean())
        })
        .asFlow()
        .mapToList()

    val currentIntervalScheme: Flow<IntervalScheme?> = queries
        .getCurrentIntervalScheme()
        .asFlow()
        .mapToOneNotNull()

    val isSaveIntervalSchemeButtonEnabled: Flow<Boolean> = currentIntervalScheme
        .map { it != null && it.id != 0L && it.name.isEmpty() }

    val isPresetNameInputDialogVisible: Flow<Boolean> = queries
        .isPresetNameInputDialogVisible()
        .asFlow()
        .mapToOne()
        .map { it.asBoolean() }

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