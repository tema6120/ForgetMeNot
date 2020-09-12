package com.odnovolov.forgetmenot.presentation.common.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class FullscreenPreference(
    isEnabledInHomeAndSettings: Boolean = false,
    isEnabledInExercise: Boolean = false,
    isEnabledInRepetition: Boolean = false
) : FlowMakerWithRegistry<FullscreenPreference>() {
    var isEnabledInHomeAndSettings: Boolean by flowMaker(isEnabledInHomeAndSettings)
    var isEnabledInExercise: Boolean by flowMaker(isEnabledInExercise)
    var isEnabledInRepetition: Boolean by flowMaker(isEnabledInRepetition)

    override fun copy() = FullscreenPreference(
        isEnabledInHomeAndSettings,
        isEnabledInExercise,
        isEnabledInRepetition
    )
}