package com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.*
import com.soywiz.klock.DateTimeSpan

class GlobalStateBuilder private constructor(private val tables: TablesForGlobalState) {
    companion object {
        fun build(tables: TablesForGlobalState): GlobalState = GlobalStateBuilder(tables).build()
    }

    private fun build(): GlobalState {
        val intervalSchemes: List<IntervalScheme> = buildIntervalSchemes()
        val pronunciations: List<Pronunciation> = buildPronunciations()
        val pronunciationPlans: List<PronunciationPlan> = buildPronunciationPlans()
        val exercisePreferences: List<ExercisePreference> =
            buildExercisePreferences(intervalSchemes, pronunciations, pronunciationPlans)
        val decks: CopyableList<Deck> = buildDecks(exercisePreferences)
        val sharedExercisePreferences: CopyableList<ExercisePreference> =
            buildSharedExercisePreferences(exercisePreferences)
        val cardFilterForAutoplay: CardFilterForAutoplay = buildCardFilterForAutoplay()
        val isWalkingModeEnabled: Boolean = buildIsWalkingModeEnabled()
        // todo
        val numberOfLapsInPlayer = 1
        return GlobalState(
            decks,
            sharedExercisePreferences,
            cardFilterForAutoplay,
            isWalkingModeEnabled,
            numberOfLapsInPlayer
        )
    }

    private fun buildIntervalSchemes(): List<IntervalScheme> {
        return tables.intervalSchemeTable
            .map { intervalSchemeId: Long ->
                val intervals: CopyableList<Interval> = tables.intervalTable
                    .filter { it.intervalSchemeId == intervalSchemeId }
                    .sortedBy { it.grade }
                    .map { it.toInterval() }
                    .toCopyableList()
                IntervalScheme(intervalSchemeId, intervals)
            }
    }

    private fun buildPronunciations(): List<Pronunciation> {
        return tables.pronunciationTable.map { it.toPronunciation() }
    }

    private fun buildPronunciationPlans(): List<PronunciationPlan> {
        return tables.pronunciationPlanTable.map { it.toPronunciationPlan() }
    }

    private fun buildExercisePreferences(
        intervalSchemes: List<IntervalScheme>,
        pronunciations: List<Pronunciation>,
        pronunciationPlans: List<PronunciationPlan>
    ): List<ExercisePreference> {
        return tables.exercisePreferenceTable
            .map { exercisePreferencesDb ->
                val intervalScheme: IntervalScheme? =
                    when (exercisePreferencesDb.intervalSchemeId) {
                        null -> null
                        IntervalScheme.Default.id -> IntervalScheme.Default
                        else -> intervalSchemes
                            .first { it.id == exercisePreferencesDb.intervalSchemeId }
                    }
                val pronunciation: Pronunciation =
                    if (exercisePreferencesDb.pronunciationId == Pronunciation.Default.id) {
                        Pronunciation.Default
                    } else {
                        pronunciations.first { it.id == exercisePreferencesDb.pronunciationId }
                    }
                val pronunciationPlan: PronunciationPlan =
                    if (exercisePreferencesDb.pronunciationPlanId == PronunciationPlan.Default.id) {
                        PronunciationPlan.Default
                    } else {
                        pronunciationPlans.first {
                            it.id == exercisePreferencesDb.pronunciationPlanId
                        }
                    }
                exercisePreferencesDb.toExercisePreference(
                    intervalScheme, pronunciation, pronunciationPlan
                )
            }
    }

    private fun buildDecks(
        exercisePreferences: List<ExercisePreference>
    ): CopyableList<Deck> {
        return tables.deckTable
            .map { deckDb ->
                val cards: CopyableList<Card> = tables.cardTable
                    .filter { it.deckId == deckDb.id }
                    .map { it.toCard() }
                    .toCopyableList()
                val exercisePreference =
                    if (deckDb.exercisePreferenceId == ExercisePreference.Default.id) {
                        ExercisePreference.Default
                    } else {
                        exercisePreferences.first { it.id == deckDb.exercisePreferenceId }
                    }
                deckDb.toDeck(cards, exercisePreference)
            }
            .toCopyableList()
    }

    private fun buildSharedExercisePreferences(
        exercisePreferences: List<ExercisePreference>
    ): CopyableList<ExercisePreference> {
        val exercisePreferencesMap: Map<Long, ExercisePreference> =
            exercisePreferences.associateBy { it.id }
        return tables.sharedExercisePreferenceTable
            .map { exercisePreferenceId: Long ->
                exercisePreferencesMap.getValue(exercisePreferenceId)
            }
            .toCopyableList()
    }

    private fun buildCardFilterForAutoplay(): CardFilterForAutoplay {
        val areCardsAvailableForExerciseIncluded: Boolean =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_CARDS_AVAILABLE_FOR_EXERCISE_INCLUDED]
                ?.toBoolean()
                ?: CardFilterForAutoplay.Default.areCardsAvailableForExerciseIncluded
        val areAwaitingCardsIncluded: Boolean =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_AWAITING_CARDS_INCLUDED]
                ?.toBoolean()
                ?: CardFilterForAutoplay.Default.areAwaitingCardsIncluded
        val areLearnedCardsIncluded: Boolean =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_ARE_LEARNED_CARDS_INCLUDED]
                ?.toBoolean()
                ?: CardFilterForAutoplay.Default.areLearnedCardsIncluded
        val gradeRangeMin: Int =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_GRADE_MIN]?.toInt()
                ?: CardFilterForAutoplay.Default.gradeRange.first
        val gradeRangeMax: Int =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_GRADE_MAX]?.toInt()
                ?: CardFilterForAutoplay.Default.gradeRange.last
        val lastTestedFromTimeAgo: DateTimeSpan? =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_LAST_TESTED_FROM_TIME_AGO]
                ?.let(dateTimeSpanAdapter::decode)
                ?: CardFilterForAutoplay.Default.lastTestedFromTimeAgo
        val lastTestedToTimeAgo: DateTimeSpan? =
            tables.keyValueTable[DbKeys.CARD_FILTER_FOR_AUTOPLAY_LAST_TESTED_TO_TIME_AGO]
                ?.let(dateTimeSpanAdapter::decode)
                ?: CardFilterForAutoplay.Default.lastTestedToTimeAgo
        return CardFilterForAutoplay(
            areCardsAvailableForExerciseIncluded,
            areAwaitingCardsIncluded,
            areLearnedCardsIncluded,
            gradeRangeMin..gradeRangeMax,
            lastTestedFromTimeAgo,
            lastTestedToTimeAgo
        )
    }


    private fun buildIsWalkingModeEnabled(): Boolean {
        return tables.keyValueTable[DbKeys.IS_WALKING_MODE_ENABLED]?.toBoolean()
            ?: GlobalState.DEFAULT_IS_WALKING_MODE_ENABLED
    }
}