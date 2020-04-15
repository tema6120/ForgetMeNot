package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModeSettingsEvent.KeyGestureActionSelected

class WalkingModeSettingsController(
    private val walkingModePreference: WalkingModePreference,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<WalkingModeSettingsEvent, Nothing>() {
    override fun handle(event: WalkingModeSettingsEvent) {
        when (event) {
            is KeyGestureActionSelected -> {
                with(walkingModePreference) {
                    keyGestureMap = keyGestureMap.toMutableMap()
                        .apply { this[event.keyGesture] = event.keyGestureAction }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}