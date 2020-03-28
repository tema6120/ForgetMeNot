package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsViewModel(
    private val repetitionSettings: RepetitionSettings
) : ViewModel() {
    val matchingCardsNumber: Flow<Int> = repetitionSettings.state.asFlow()
        .map { repetitionSettings.getCurrentMatchingCardsNumber() }

    val isAvailableForExerciseGroupChecked: Flow<Boolean> = repetitionSettings.state.flowOf(
        RepetitionSettings.State::isAvailableForExerciseCardsIncluded
    )

    val isAwaitingGroupChecked: Flow<Boolean> = repetitionSettings.state.flowOf(
        RepetitionSettings.State::isAwaitingCardsIncluded
    )

    val isLearnedGroupChecked: Flow<Boolean> = repetitionSettings.state.flowOf(
        RepetitionSettings.State::isLearnedCardsIncluded
    )

    val availableLevelOfKnowledgeRange: IntRange = run {
        val allLevelOfKnowledge: List<Int> = repetitionSettings.state.decks
            .flatMap { it.cards }
            .map { it.levelOfKnowledge }
        val min: Int = allLevelOfKnowledge.min()!!
        val max: Int = allLevelOfKnowledge.max()!!
        min..max
    }

    val currentLevelOfKnowledgeRange: IntRange
        get() = repetitionSettings.state.levelOfKnowledgeRange

    val lastAnswerFromTimeAgo: Flow<DisplayedInterval?> = repetitionSettings.state.flowOf(
        RepetitionSettings.State::lastAnswerFromTimeAgo
    ).map { dateTimeSpan: DateTimeSpan? ->
        dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
    }

    val lastAnswerToTimeAgo: Flow<DisplayedInterval?> = repetitionSettings.state.flowOf(
        RepetitionSettings.State::lastAnswerToTimeAgo
    ).map { dateTimeSpan: DateTimeSpan? ->
        dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
    }

    val numberOfLaps: Flow<Int> =
        repetitionSettings.state.flowOf(RepetitionSettings.State::numberOfLaps)

    override fun onCleared() {
        getKoin().getScope(REPETITION_SETTINGS_SCOPE_ID).close()
    }
}