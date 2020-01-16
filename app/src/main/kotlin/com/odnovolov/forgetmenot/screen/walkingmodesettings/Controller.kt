package com.odnovolov.forgetmenot.screen.walkingmodesettings

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.screen.walkingmodesettings.WalkingModeSettingsEvent.KeyGestureActionSelected

class WalkingModeSettingsController : BaseController<WalkingModeSettingsEvent, Nothing>() {
    private val queries: WalkingModeSettingsControllerQueries =
        database.walkingModeSettingsControllerQueries

    override fun handleEvent(event: WalkingModeSettingsEvent) {
        when (event) {
            is KeyGestureActionSelected -> {
                queries.setKeyGestureAction(event.keyGestureAction, event.keyGesture)
            }
        }
    }
}