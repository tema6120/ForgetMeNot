package com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference

class WalkingModePreferenceProvider(
    private val database: Database
) : LongTermStateProvider<WalkingModePreference> {
    override fun load(): WalkingModePreference {
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