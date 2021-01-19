package com.odnovolov.forgetmenot.presentation.common.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class FullscreenPreference(
    isEnabledInExercise: Boolean,
    isEnabledInCardPlayer: Boolean,
    isEnabledInOtherPlaces: Boolean
) : FlowMakerWithRegistry<FullscreenPreference>() {
    var isEnabledInExercise: Boolean by flowMaker(isEnabledInExercise)
    var isEnabledInCardPlayer: Boolean by flowMaker(isEnabledInCardPlayer)
    var isEnabledInOtherPlaces: Boolean by flowMaker(isEnabledInOtherPlaces)

    override fun copy() = FullscreenPreference(
        isEnabledInExercise,
        isEnabledInCardPlayer,
        isEnabledInOtherPlaces
    )

    companion object {
        const val DEFAULT_IS_ENABLED_IN_EXERCISE = false
        const val DEFAULT_IS_ENABLED_IN_CARD_PLAYER = false
        const val DEFAULT_IS_ENABLED_IN_OTHER_PLACES = false
    }
}