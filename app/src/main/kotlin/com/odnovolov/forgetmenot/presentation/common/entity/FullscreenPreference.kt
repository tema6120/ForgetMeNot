package com.odnovolov.forgetmenot.presentation.common.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class FullscreenPreference(
    isEnabledInHomeAndSettings: Boolean = false,
    isEnabledInExercise: Boolean = false,
    isEnabledInRepetition: Boolean = false
) : RegistrableFlowableState<FullscreenPreference>() {
    var isEnabledInHomeAndSettings: Boolean by me(isEnabledInHomeAndSettings)
    var isEnabledInExercise: Boolean by me(isEnabledInExercise)
    var isEnabledInRepetition: Boolean by me(isEnabledInRepetition)

    override fun copy() = FullscreenPreference(
        isEnabledInHomeAndSettings,
        isEnabledInExercise,
        isEnabledInRepetition
    )
}