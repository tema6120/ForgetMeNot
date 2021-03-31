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
        }
    }
}