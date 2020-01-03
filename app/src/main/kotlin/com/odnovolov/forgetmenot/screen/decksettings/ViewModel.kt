package com.odnovolov.forgetmenot.screen.decksettings

import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.common.database.*
import com.odnovolov.forgetmenot.common.entity.CardReverse
import com.odnovolov.forgetmenot.common.entity.TestMethod
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
        .getAvailableExercisePreferences(::Preset)
        .asFlow()
        .mapToList()

    val isDialogVisible: Flow<Boolean> = queries
        .isDialogVisible()
        .asFlow()
        .mapToOne()

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

    val selectedCardReverse: Flow<CardReverse> = queries
        .getSelectedCardReverse()
        .asFlow()
        .mapToOne()
}