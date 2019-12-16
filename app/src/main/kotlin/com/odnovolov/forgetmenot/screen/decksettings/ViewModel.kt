package com.odnovolov.forgetmenot.screen.decksettings

import com.odnovolov.forgetmenot.common.IntervalScheme
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import com.odnovolov.forgetmenot.decksettings.DeckSettingsViewModelQueries
import com.odnovolov.forgetmenot.decksettings.ExercisePreferenceIdAndName
import com.odnovolov.forgetmenot.decksettings.PronunciationIdAndName
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

    val availableExercisePreferences: Flow<List<Preset>> = queries
        .getAvailableExercisePreferences(mapper = { id: Long, name: String, isSelected: Long ->
            Preset(id, name, isSelected.asBoolean())
        })
        .asFlow()
        .mapToList()

    val isDialogVisible: Flow<Boolean> = queries
        .isDialogVisible()
        .asFlow()
        .mapToOne()
        .map { it.asBoolean() }

    val dialogInputCheckResult: Flow<NameCheckResult> = queries
        .getDialogInputCheckResult()
        .asFlow()
        .mapToOne()
        .map { databaseValue: String -> nameCheckResultAdapter.decode(databaseValue) }

    val randomOrder: Flow<Boolean> = queries
        .getRandomOrder()
        .asFlow()
        .mapToOne()

    val selectedTestMethod: Flow<TestMethod> = queries
        .getSelectedTestMethod()
        .asFlow()
        .mapToOne()

    val intervalScheme: Flow<IntervalScheme?> = queries
        .getIntervalScheme()
        .asFlow()
        .mapToOneNotNull()

    val pronunciationIdAndName: Flow<PronunciationIdAndName> = queries
        .pronunciationIdAndName()
        .asFlow()
        .mapToOne()

    val isQuestionDisplayed: Flow<Boolean> = queries
        .isQuestionDisplayed()
        .asFlow()
        .mapToOne()
}