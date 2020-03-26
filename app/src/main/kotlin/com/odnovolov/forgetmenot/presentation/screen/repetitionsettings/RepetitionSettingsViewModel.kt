package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsViewModel(
    private val repetitionSettingsState: RepetitionSettings.State
) : ViewModel() {
    val isAvailableForExerciseGroupChecked: Flow<Boolean> = repetitionSettingsState.flowOf(
        RepetitionSettings.State::isAvailableForExerciseCardsIncluded
    )

    val isAwaitingGroupChecked: Flow<Boolean> = repetitionSettingsState.flowOf(
        RepetitionSettings.State::isAwaitingCardsIncluded
    )

    val isLearnedGroupChecked: Flow<Boolean> = repetitionSettingsState.flowOf(
        RepetitionSettings.State::isLearnedCardsIncluded
    )

    val availableLevelOfKnowledgeRange: IntRange = run {
        val allLevelOfKnowledge: List<Int> = repetitionSettingsState.decks
            .flatMap { it.cards }
            .map { it.levelOfKnowledge }
        val min: Int = allLevelOfKnowledge.min()!!
        val max: Int = allLevelOfKnowledge.max()!!
        min..max
    }

    val currentLevelOfKnowledgeRange: IntRange
        get() = repetitionSettingsState.levelOfKnowledgeRange

    val lastAnswerFromTimeAgo: Flow<DisplayedInterval?> = repetitionSettingsState.flowOf(
        RepetitionSettings.State::lastAnswerFromTimeAgo
    ).map { dateTimeSpan: DateTimeSpan? ->
        dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
    }

    val lastAnswerToTimeAgo: Flow<DisplayedInterval?> = repetitionSettingsState.flowOf(
        RepetitionSettings.State::lastAnswerToTimeAgo
    ).map { dateTimeSpan: DateTimeSpan? ->
        dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
    }

    override fun onCleared() {
        getKoin().getScope(REPETITION_SETTINGS_SCOPE_ID).close()
    }
}