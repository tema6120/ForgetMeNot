package com.odnovolov.forgetmenot.screen.walkingmodesettings

import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction

sealed class WalkingModeSettingsEvent {
    class KeyGestureActionSelected(
        val keyGesture: KeyGesture,
        val keyGestureAction: KeyGestureAction
    ) : WalkingModeSettingsEvent()
}