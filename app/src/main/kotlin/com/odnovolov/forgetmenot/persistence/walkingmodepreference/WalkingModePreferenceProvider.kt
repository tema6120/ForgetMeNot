package com.odnovolov.forgetmenot.persistence.walkingmodepreference

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

object WalkingModePreferenceProvider {
    fun load(): WalkingModePreference {
        lateinit var keyGestureMap: Map<KeyGesture, KeyGestureAction>
        database.transaction {
            keyGestureMap = database.keyGestureMapQueries
                .selectAll()
                .executeAsList()
                .associate { it.keyGesture to it.keyGestureAction }
            }
        return if (keyGestureMap.isEmpty()) {
            WalkingModePreference()
        } else {
            WalkingModePreference(keyGestureMap)
        }
    }
}