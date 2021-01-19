package com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.KeyValue
import com.odnovolov.forgetmenot.persistence.globalstate.*

class TablesForGlobalState private constructor(
    val deckTable: List<DeckDb>,
    val cardTable: List<CardDb>,
    val exercisePreferenceTable: List<ExercisePreferenceDb>,
    val intervalSchemeTable: List<Long>,
    val intervalTable: List<IntervalDb>,
    val pronunciationTable: List<PronunciationDb>,
    val pronunciationPlanTable: List<PronunciationPlanDb>,
    val sharedExercisePreferenceTable: List<Long>,
    val keyValueTable: Map<Long, String?>
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
                    pronunciationPlanTable = pronunciationPlanQueries.selectAll().executeAsList(),
                    sharedExercisePreferenceTable = sharedExercisePreferenceQueries.selectAll().executeAsList(),
                    keyValueTable = keyValueQueries.selectAll().executeAsList().associate { it.key to it.value }
                )
            }
        }
    }
}