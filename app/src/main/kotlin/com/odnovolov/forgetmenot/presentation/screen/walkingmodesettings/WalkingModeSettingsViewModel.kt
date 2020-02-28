package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.KoinComponent

class WalkingModeSettingsViewModel(
    walkingModePreference: WalkingModePreference
) : ViewModel(), KoinComponent {
    private val keyGestureMap: Flow<Map<KeyGesture, KeyGestureAction>> = walkingModePreference
        .flowOf(WalkingModePreference::keyGestureMap)
        .share()

    val selectedVolumeUpSinglePressAction: Flow<KeyGestureAction> = keyGestureMap
        .map { it.getValue(VOLUME_UP_SINGLE_PRESS) }
        .distinctUntilChanged()

    val selectedVolumeUpDoublePressAction: Flow<KeyGestureAction> = keyGestureMap
        .map { it.getValue(VOLUME_UP_DOUBLE_PRESS) }
        .distinctUntilChanged()

    val selectedVolumeUpLongPressAction: Flow<KeyGestureAction> = keyGestureMap
        .map { it.getValue(VOLUME_UP_LONG_PRESS) }
        .distinctUntilChanged()

    val selectedVolumeDownSinglePressAction: Flow<KeyGestureAction> = keyGestureMap
        .map { it.getValue(VOLUME_DOWN_SINGLE_PRESS) }
        .distinctUntilChanged()

    val selectedVolumeDownDoublePressAction: Flow<KeyGestureAction> = keyGestureMap
        .map { it.getValue(VOLUME_DOWN_DOUBLE_PRESS) }
        .distinctUntilChanged()

    val selectedVolumeDownLongPressAction: Flow<KeyGestureAction> = keyGestureMap
        .map { it.getValue(VOLUME_DOWN_LONG_PRESS) }
        .distinctUntilChanged()

    override fun onCleared() {
        getKoin().getScope(WALKING_MODE_SETTINGS_MODULE_SCOPE_ID).close()
    }
}