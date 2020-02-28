package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.presentation.common.Store

class WalkingModeSettingsController(
    private val walkingModePreference: WalkingModePreference,
    private val store: Store
) {
    fun onKeyGestureActionSelected(keyGesture: KeyGesture, keyGestureAction: KeyGestureAction) {
        with(walkingModePreference) {
            keyGestureMap = keyGestureMap.toMutableMap()
                .apply { this[keyGesture] = keyGestureAction }
        }
        store.saveStateByRegistry()
    }
}