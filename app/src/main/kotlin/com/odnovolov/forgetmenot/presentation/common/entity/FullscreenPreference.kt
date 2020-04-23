package com.odnovolov.forgetmenot.presentation.common.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class FullscreenPreference(
    isEnabledInDashboardAndSettings: Boolean = false,
    isEnabledInExercise: Boolean = false,
    isEnabledInRepetition: Boolean = false
) : RegistrableFlowableState<FullscreenPreference>() {
    var isEnabledInDashboardAndSettings: Boolean by me(isEnabledInDashboardAndSettings)
    var isEnabledInExercise: Boolean by me(isEnabledInExercise)
    var isEnabledInRepetition: Boolean by me(isEnabledInRepetition)

    override fun copy() = FullscreenPreference(
        isEnabledInDashboardAndSettings,
        isEnabledInExercise,
        isEnabledInRepetition
    )
}