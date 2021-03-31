package com.odnovolov.forgetmenot.persistence.longterm.exercisesettings

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardPrefilterMode
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings
import kotlinx.serialization.json.Json

class ExerciseSettingsProvider(
    private val database: Database,
    private val json: Json
) : LongTermStateProvider<ExerciseSettings> {
    override fun load(): ExerciseSettings {
        val keyValues: Map<Long, String?> = database.keyValueQueries
            .selectValues(
                keys = listOf(
                    DbKeys.CARD_PREFILTER_MODE
                )
            )
            .executeAsList()
            .associate { (key, value) -> key to value }
        val cardPrefilterMode: CardPrefilterMode = keyValues[DbKeys.CARD_PREFILTER_MODE]
            ?.let { dbValue: String ->
                json.decodeFromString(CardPrefilterMode.serializer(), dbValue)
            }
            ?: ExerciseSettings.DEFAULT_CARD_PREFILTER_MODE
        return ExerciseSettings(
            cardPrefilterMode
        )
    }
}