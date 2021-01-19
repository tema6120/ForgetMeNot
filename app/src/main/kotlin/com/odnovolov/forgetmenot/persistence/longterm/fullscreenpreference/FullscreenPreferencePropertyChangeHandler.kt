package com.odnovolov.forgetmenot.persistence.longterm.fullscreenpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference

class FullscreenPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            FullscreenPreference::isEnabledInExercise -> {
                val isEnabledInExercise = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.IS_FULLSCREEN_ENABLED_IN_EXERCISE,
                    value = isEnabledInExercise.toString()
                )
            }
            FullscreenPreference::isEnabledInCardPlayer -> {
                val isEnabledInCardPlayer = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.IS_FULLSCREEN_ENABLED_IN_CARD_PLAYER,
                    value = isEnabledInCardPlayer.toString()
                )
            }
            FullscreenPreference::isEnabledInOtherPlaces -> {
                val isEnabledInOtherPlaces = change.newValue as Boolean
                queries.replace(
                    key = DbKeys.IS_FULLSCREEN_ENABLED_IN_OTHER_PLACES,
                    value = isEnabledInOtherPlaces.toString()
                )
            }
        }
    }
}