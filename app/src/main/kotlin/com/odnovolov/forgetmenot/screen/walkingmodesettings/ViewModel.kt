package com.odnovolov.forgetmenot.screen.walkingmodesettings

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.entity.KeyGesture.*
import com.odnovolov.forgetmenot.common.entity.KeyGestureAction
import kotlinx.coroutines.flow.Flow

class WalkingModeSettingsViewModel {
    private val queries: WalkingModeSettingsViewModelQueries =
        database.walkingModeSettingsViewModelQueries

    val selectedVolumeUpSinglePressAction: Flow<KeyGestureAction> = queries
        .selectedKeyGestureAction(VOLUME_UP_SINGLE_PRESS)
        .asFlow()
        .mapToOne()

    val selectedVolumeUpDoublePressAction: Flow<KeyGestureAction> = queries
        .selectedKeyGestureAction(VOLUME_UP_DOUBLE_PRESS)
        .asFlow()
        .mapToOne()

    val selectedVolumeUpLongPressAction: Flow<KeyGestureAction> = queries
        .selectedKeyGestureAction(VOLUME_UP_LONG_PRESS)
        .asFlow()
        .mapToOne()

    val selectedVolumeDownSinglePressAction: Flow<KeyGestureAction> = queries
        .selectedKeyGestureAction(VOLUME_DOWN_SINGLE_PRESS)
        .asFlow()
        .mapToOne()

    val selectedVolumeDownDoublePressAction: Flow<KeyGestureAction> = queries
        .selectedKeyGestureAction(VOLUME_DOWN_DOUBLE_PRESS)
        .asFlow()
        .mapToOne()

    val selectedVolumeDownLongPressAction: Flow<KeyGestureAction> = queries
        .selectedKeyGestureAction(VOLUME_DOWN_LONG_PRESS)
        .asFlow()
        .mapToOne()
}