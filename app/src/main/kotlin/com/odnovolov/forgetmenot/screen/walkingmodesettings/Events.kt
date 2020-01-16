package com.odnovolov.forgetmenot.screen.walkingmodesettings

import com.odnovolov.forgetmenot.common.entity.KeyGesture
import com.odnovolov.forgetmenot.common.entity.KeyGestureAction

sealed class WalkingModeSettingsEvent {
    class KeyGestureActionSelected(
        val keyGesture: KeyGesture,
        val keyGestureAction: KeyGestureAction
    ) : WalkingModeSettingsEvent()
}