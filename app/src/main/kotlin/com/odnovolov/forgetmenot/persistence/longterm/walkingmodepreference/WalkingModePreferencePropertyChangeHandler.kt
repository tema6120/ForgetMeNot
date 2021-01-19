package com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.KeyGestureMapDb
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

class WalkingModePreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyGestureMapQueries

    override fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            WalkingModePreference::keyGestureMap -> {
                val keyGestureMap = change.newValue as Map<KeyGesture, KeyGestureAction>
                keyGestureMap.forEach { (keyGesture: KeyGesture, keyGestureAction: KeyGestureAction) ->
                    val keyGestureMapDb = KeyGestureMapDb(keyGesture, keyGestureAction)
                    queries.replace(keyGestureMapDb)
                }
            }
        }
    }
}