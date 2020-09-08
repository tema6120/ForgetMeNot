package com.odnovolov.forgetmenot.persistence.longterm.fullscreenpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference

class FullscreenPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.fullscreenPreferenceQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            FullscreenPreference::isEnabledInHomeAndSettings -> {
                val isEnabledInHomeAndSettings = change.newValue as Boolean
                queries.updateIsEnabledInHomeAndSettings(isEnabledInHomeAndSettings)
            }
            FullscreenPreference::isEnabledInExercise -> {
                val isEnabledInExercise = change.newValue as Boolean
                queries.updateIsEnabledInExercise(isEnabledInExercise)
            }
            FullscreenPreference::isEnabledInRepetition -> {
                val isEnabledInRepetition = change.newValue as Boolean
                queries.updateIsEnabledInRepetition(isEnabledInRepetition)
            }
        }
    }
}