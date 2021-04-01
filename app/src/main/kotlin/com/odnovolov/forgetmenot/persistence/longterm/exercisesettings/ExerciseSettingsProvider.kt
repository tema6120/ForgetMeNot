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
                    DbKeys.CARD_PREFILTER_MODE,
                    DbKeys.SHOW_PROGRESS_BAR_IN_EXERCISE,
                    DbKeys.SHOW_TEXT_OF_CARD_POSITION_IN_EXERCISE,
                    DbKeys.VIBRATE_ON_WRONG_ANSWER,
                    DbKeys.GO_TO_NEXT_CARD_AFTER_MARKING_AS_LEARNED,
                    DbKeys.ASK_TO_QUIT_EXERCISE
                )
            )
            .executeAsList()
            .associate { (key, value) -> key to value }
        val cardPrefilterMode: CardPrefilterMode = keyValues[DbKeys.CARD_PREFILTER_MODE]
            ?.let { dbValue: String ->
                json.decodeFromString(CardPrefilterMode.serializer(), dbValue)
            }
            ?: ExerciseSettings.DEFAULT_CARD_PREFILTER_MODE
        val showProgressBar: Boolean = keyValues[DbKeys.SHOW_PROGRESS_BAR_IN_EXERCISE]
            ?.toBoolean()
            ?: ExerciseSettings.DEFAULT_SHOW_PROGRESS_BAR
        val showTextOfCardPosition: Boolean =
            keyValues[DbKeys.SHOW_TEXT_OF_CARD_POSITION_IN_EXERCISE]
                ?.toBoolean()
                ?: ExerciseSettings.DEFAULT_SHOW_TEXT_OF_CARD_POSITION
        val vibrateOnWrongAnswer: Boolean = keyValues[DbKeys.VIBRATE_ON_WRONG_ANSWER]
            ?.toBoolean()
            ?: ExerciseSettings.DEFAULT_VIBRATE_ON_WRONG_ANSWER
        val goToNextCardAfterMarkingAsLearned: Boolean =
            keyValues[DbKeys.GO_TO_NEXT_CARD_AFTER_MARKING_AS_LEARNED]
                ?.toBoolean()
                ?: ExerciseSettings.DEFAULT_GO_TO_NEXT_CARD_AFTER_MARKING_AS_LEARNED
        val askToQuit: Boolean = keyValues[DbKeys.ASK_TO_QUIT_EXERCISE]
                ?.toBoolean()
                ?: ExerciseSettings.DEFAULT_ASK_TO_QUIT
        return ExerciseSettings(
            cardPrefilterMode,
            showProgressBar,
            showTextOfCardPosition,
            vibrateOnWrongAnswer,
            goToNextCardAfterMarkingAsLearned,
            askToQuit
        )
    }
}