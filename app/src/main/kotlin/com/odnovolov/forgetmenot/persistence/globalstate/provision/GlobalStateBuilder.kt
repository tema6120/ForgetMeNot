package com.odnovolov.forgetmenot.persistence.globalstate.provision

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.*

class GlobalStateBuilder private constructor(private val tables: TablesForGlobalState) {
    companion object {
        fun build(tables: TablesForGlobalState): GlobalState = GlobalStateBuilder(tables).build()
    }

    private fun build(): GlobalState {
        val intervalSchemes: List<IntervalScheme> = buildIntervalSchemes()
        val pronunciations: List<Pronunciation> = buildPronunciations()
        val exercisePreferences: List<ExercisePreference> =
            buildExercisePreferences(intervalSchemes, pronunciations)
        val decks: CopyableList<Deck> = buildDecks(exercisePreferences)
        val sharedExercisePreferences: CopyableList<ExercisePreference> =
            buildSharedExercisePreferences(exercisePreferences)
        val sharedIntervalSchemes: CopyableList<IntervalScheme> =
            buildSharedIntervalSchemes(intervalSchemes)
        return GlobalState(decks, sharedExercisePreferences, sharedIntervalSchemes)
    }

    private fun buildIntervalSchemes(): List<IntervalScheme> {
        return tables.intervalSchemeTable
            .map { intervalSchemeDb ->
                val intervals: CopyableList<Interval> = tables.intervalTable
                    .filter { it.intervalSchemeId == intervalSchemeDb.id }
                    .map { it.toInterval() }
                    .toCopyableList()
                intervalSchemeDb.toIntervalScheme(intervals)
            }
    }

    private fun buildPronunciations(): List<Pronunciation> {
        return tables.pronunciationTable
            .map { it.toPronunciation() }
    }

    private fun buildExercisePreferences(
        intervalSchemes: List<IntervalScheme>,
        pronunciations: List<Pronunciation>
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
                exercisePreferencesDb.toExercisePreference(intervalScheme, pronunciation)
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

    private fun buildSharedIntervalSchemes(
        intervalSchemes: List<IntervalScheme>
    ): CopyableList<IntervalScheme> {
        val intervalSchemesMap: Map<Long, IntervalScheme> = intervalSchemes.associateBy { it.id }
        return tables.sharedIntervalSchemeTable
            .map { intervalSchemeId: Long ->
                intervalSchemesMap.getValue(intervalSchemeId)
            }
            .toCopyableList()
    }
}