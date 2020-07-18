package com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.persistence.*
import com.odnovolov.forgetmenot.persistence.globalstate.RepetitionSettingDb

class GlobalStateBuilder private constructor(private val tables: TablesForGlobalState) {
    companion object {
        fun build(tables: TablesForGlobalState): GlobalState = GlobalStateBuilder(tables).build()
    }

    private fun build(): GlobalState {
        val intervalSchemes: List<IntervalScheme> = buildIntervalSchemes()
        val pronunciations: List<Pronunciation> = buildPronunciations()
        val speakPlans: List<SpeakPlan> = buildSpeakPlans()
        val exercisePreferences: List<ExercisePreference> =
            buildExercisePreferences(intervalSchemes, pronunciations, speakPlans)
        val decks: CopyableList<Deck> = buildDecks(exercisePreferences)
        val sharedExercisePreferences: CopyableList<ExercisePreference> =
            buildSharedExercisePreferences(exercisePreferences)
        val sharedIntervalSchemes: CopyableList<IntervalScheme> =
            buildSharedIntervalSchemes(intervalSchemes)
        val sharedPronunciations: CopyableList<Pronunciation> =
            buildSharedPronunciations(pronunciations)
        val sharedSpeakPlans: CopyableList<SpeakPlan> = buildSharedSpeakPlans(speakPlans)
        val repetitionSettings: CopyableList<RepetitionSetting> = buildRepetitionSettings()
        val sharedRepetitionSettings: CopyableList<RepetitionSetting> =
            buildSharedRepetitionSettings(repetitionSettings)
        val currentRepetitionSetting: RepetitionSetting =
            buildCurrentRepetitionSetting(repetitionSettings)
        val isWalkingModeEnabled: Boolean = tables.walkingModeTable
        return GlobalState(
            decks,
            sharedExercisePreferences,
            sharedIntervalSchemes,
            sharedPronunciations,
            sharedSpeakPlans,
            sharedRepetitionSettings,
            currentRepetitionSetting,
            isWalkingModeEnabled
        )
    }

    private fun buildIntervalSchemes(): List<IntervalScheme> {
        return tables.intervalSchemeTable
            .map { intervalSchemeDb ->
                val intervals: CopyableList<Interval> = tables.intervalTable
                    .filter { it.intervalSchemeId == intervalSchemeDb.id }
                    .sortedBy { it.levelOfKnowledge }
                    .map { it.toInterval() }
                    .toCopyableList()
                intervalSchemeDb.toIntervalScheme(intervals)
            }
    }

    private fun buildPronunciations(): List<Pronunciation> {
        return tables.pronunciationTable.map { it.toPronunciation() }
    }

    private fun buildSpeakPlans(): List<SpeakPlan> {
        return tables.speakPlanTable.map { it.toSpeakPlan() }
    }

    private fun buildExercisePreferences(
        intervalSchemes: List<IntervalScheme>,
        pronunciations: List<Pronunciation>,
        speakPlans: List<SpeakPlan>
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
                val speakPlan: SpeakPlan =
                    if (exercisePreferencesDb.speakPlanId == SpeakPlan.Default.id) {
                        SpeakPlan.Default
                    } else {
                        speakPlans.first { it.id == exercisePreferencesDb.speakPlanId }
                    }
                exercisePreferencesDb.toExercisePreference(intervalScheme, pronunciation, speakPlan)
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
            .map { intervalSchemeId: Long -> intervalSchemesMap.getValue(intervalSchemeId) }
            .toCopyableList()
    }

    private fun buildSharedPronunciations(
        pronunciations: List<Pronunciation>
    ): CopyableList<Pronunciation> {
        val pronunciationsMap: Map<Long, Pronunciation> = pronunciations.associateBy { it.id }
        return tables.sharedPronunciationTable
            .map { pronunciationId: Long -> pronunciationsMap.getValue(pronunciationId) }
            .toCopyableList()
    }

    private fun buildSharedSpeakPlans(speakPlans: List<SpeakPlan>): CopyableList<SpeakPlan> {
        val speakPlanMap: Map<Long, SpeakPlan> = speakPlans.associateBy { it.id }
        return tables.sharedSpeakPlanTable
            .map { speakPlanId: Long -> speakPlanMap.getValue(speakPlanId) }
            .toCopyableList()
    }

    private fun buildRepetitionSettings(): CopyableList<RepetitionSetting> {
        return tables.repetitionSettingTable
            .map { repetitionSettingDb: RepetitionSettingDb ->
                repetitionSettingDb.toRepetitionSetting()
            }
            .toCopyableList()
    }

    private fun buildSharedRepetitionSettings(
        repetitionSettings: CopyableList<RepetitionSetting>
    ): CopyableList<RepetitionSetting> {
        val repetitionSettingsMap: Map<Long, RepetitionSetting> =
            repetitionSettings.associateBy { it.id }
        return tables.sharedRepetitionSettingTable
            .map { repetitionSettingId: Long ->
                repetitionSettingsMap.getValue(repetitionSettingId)
            }
            .toCopyableList()
    }

    private fun buildCurrentRepetitionSetting(
        repetitionSettings: CopyableList<RepetitionSetting>
    ): RepetitionSetting {
        val currentRepetitionSettingId: Long = tables.currentRepetitionSettingTable
        return if (currentRepetitionSettingId == RepetitionSetting.Default.id) {
            RepetitionSetting.Default
        } else {
            repetitionSettings.find { it.id == currentRepetitionSettingId }!!
        }
    }
}