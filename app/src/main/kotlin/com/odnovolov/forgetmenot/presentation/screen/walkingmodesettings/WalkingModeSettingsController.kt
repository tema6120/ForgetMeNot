package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver

class WalkingModeSettingsController(
    private val walkingModePreference: WalkingModePreference,
    private val longTermStateSaver: LongTermStateSaver
) {
    fun onKeyGestureActionSelected(keyGesture: KeyGesture, keyGestureAction: KeyGestureAction) {
        with(walkingModePreference) {
            keyGestureMap = keyGestureMap.toMutableMap()
                .apply { this[keyGesture] = keyGestureAction }
        }
        longTermStateSaver.saveStateByRegistry()
    }
}