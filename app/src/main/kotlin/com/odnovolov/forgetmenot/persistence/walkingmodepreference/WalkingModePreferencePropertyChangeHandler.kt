package com.odnovolov.forgetmenot.persistence.walkingmodepreference

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.KeyGestureMapDb
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

object WalkingModePreferencePropertyChangeHandler {
    fun handle(change: PropertyChangeRegistry.Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            WalkingModePreference::keyGestureMap -> {
                val keyGestureMap = change.newValue as Map<KeyGesture, KeyGestureAction>
                keyGestureMap.forEach { (keyGesture: KeyGesture, keyGestureAction: KeyGestureAction) ->
                    val keyGestureMapDb: KeyGestureMapDb =
                        KeyGestureMapDb.Impl(keyGesture, keyGestureAction)
                    database.keyGestureMapQueries.replace(keyGestureMapDb)
                }
            }
        }
    }
}