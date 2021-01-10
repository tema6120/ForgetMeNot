package com.odnovolov.forgetmenot.presentation.common.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class FullscreenPreference(
    isEnabledInExercise: Boolean = false,
    isEnabledInCardPlayer: Boolean = false,
    isEnabledInOtherPlaces: Boolean = false
) : FlowMakerWithRegistry<FullscreenPreference>() {
    var isEnabledInExercise: Boolean by flowMaker(isEnabledInExercise)
    var isEnabledInCardPlayer: Boolean by flowMaker(isEnabledInCardPlayer)
    var isEnabledInOtherPlaces: Boolean by flowMaker(isEnabledInOtherPlaces)

    override fun copy() = FullscreenPreference(
        isEnabledInExercise,
        isEnabledInCardPlayer,
        isEnabledInOtherPlaces
    )
}