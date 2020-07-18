package com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.globalstate.*

class TablesForGlobalState private constructor(
    val deckTable: List<DeckDb>,
    val cardTable: List<CardDb>,
    val exercisePreferenceTable: List<ExercisePreferenceDb>,
    val intervalSchemeTable: List<IntervalSchemeDb>,
    val intervalTable: List<IntervalDb>,
    val pronunciationTable: List<PronunciationDb>,
    val speakPlanTable: List<SpeakPlanDb>,
    val sharedExercisePreferenceTable: List<Long>,
    val sharedIntervalSchemeTable: List<Long>,
    val sharedPronunciationTable: List<Long>,
    val sharedSpeakPlanTable: List<Long>,
    val repetitionSettingTable: List<RepetitionSettingDb>,
    val sharedRepetitionSettingTable: List<Long>,
    val currentRepetitionSettingTable: Long,
    val walkingModeTable: Boolean
) {
    companion object {
        fun load(database: Database): TablesForGlobalState {
            return with(database) {
                TablesForGlobalState(
                    deckTable = deckQueries.selectAll().executeAsList(),
                    cardTable = cardQueries.selectAll().executeAsList(),
                    exercisePreferenceTable = exercisePreferenceQueries.selectAll().executeAsList(),
                    intervalSchemeTable = intervalSchemeQueries.selectAll().executeAsList(),
                    intervalTable = intervalQueries.selectAll().executeAsList(),
                    pronunciationTable = pronunciationQueries.selectAll().executeAsList(),
                    speakPlanTable = speakPlanQueries.selectAll().executeAsList(),
                    sharedExercisePreferenceTable = sharedExercisePreferenceQueries.selectAll().executeAsList(),
                    sharedIntervalSchemeTable = sharedIntervalSchemeQueries.selectAll().executeAsList(),
                    sharedPronunciationTable = sharedPronunciationQueries.selectAll().executeAsList(),
                    sharedSpeakPlanTable = sharedSpeakPlanQueries.selectAll().executeAsList(),
                    repetitionSettingTable = repetitionSettingQueries.selectAll().executeAsList(),
                    sharedRepetitionSettingTable = sharedRepetitionSettingQueries.selectAll().executeAsList(),
                    currentRepetitionSettingTable = currentRepetitionSettingQueries.select().executeAsOne(),
                    walkingModeTable = walkingModeQueries.selectAll().executeAsOne()
                )
            }
        }
    }
}