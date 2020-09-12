package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*

class WalkingModePreference(
    keyGestureMap: Map<KeyGesture, KeyGestureAction> = mapOf(
        VOLUME_UP_SINGLE_PRESS to MARK_AS_REMEMBER,
        VOLUME_UP_DOUBLE_PRESS to MOVE_TO_NEXT_CARD,
        VOLUME_UP_LONG_PRESS to SPEAK_QUESTION,
        VOLUME_DOWN_SINGLE_PRESS to MARK_AS_NOT_REMEMBER,
        VOLUME_DOWN_DOUBLE_PRESS to MOVE_TO_PREVIOUS_CARD,
        VOLUME_DOWN_LONG_PRESS to SPEAK_ANSWER
    )
) : FlowMakerWithRegistry<WalkingModePreference>() {
    var keyGestureMap: Map<KeyGesture, KeyGestureAction> by flowMaker(keyGestureMap)

    override fun copy() = WalkingModePreference(keyGestureMap)
}