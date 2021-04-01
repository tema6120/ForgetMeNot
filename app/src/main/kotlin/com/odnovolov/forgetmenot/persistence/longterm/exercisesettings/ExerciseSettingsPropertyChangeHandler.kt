package com.odnovolov.forgetmenot.persistence.longterm.exercisesettings

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardPrefilterMode
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings
import kotlinx.serialization.json.Json

class ExerciseSettingsPropertyChangeHandler(
    database: Database,
    private val json: Json
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            ExerciseSettings::cardPrefilterMode -> {
                val cardPrefilterMode = change.newValue as CardPrefilterMode
                val serialized: String =
                    json.encodeToString(CardPrefilterMode.serializer(), cardPrefilterMode)
                queries.replace(
                    key = DbKeys.CARD_PREFILTER_MODE,
                    value = serialized
                )
            }
            ExerciseSettings::showProgressBar -> {
                val showProgressBar = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.SHOW_PROGRESS_BAR_IN_EXERCISE,
                    value = showProgressBar.toString()
                )
            }
            ExerciseSettings::showTextOfCardPosition -> {
                val showTextOfCardPosition = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.SHOW_TEXT_OF_CARD_POSITION_IN_EXERCISE,
                    value = showTextOfCardPosition.toString()
                )
            }
            ExerciseSettings::vibrateOnWrongAnswer -> {
                val vibrateOnWrongAnswer = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.VIBRATE_ON_WRONG_ANSWER,
                    value = vibrateOnWrongAnswer.toString()
                )
            }
            ExerciseSettings::goToNextCardAfterMarkingAsLearned -> {
                val goToNextCardAfterMarkingAsLearned = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.GO_TO_NEXT_CARD_AFTER_MARKING_AS_LEARNED,
                    value = goToNextCardAfterMarkingAsLearned.toString()
                )
            }
            ExerciseSettings::askToQuit -> {
                val askToQuit = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.ASK_TO_QUIT_EXERCISE,
                    value = askToQuit.toString()
                )
            }
        }
    }
}