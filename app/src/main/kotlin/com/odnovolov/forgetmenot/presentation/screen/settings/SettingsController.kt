package com.odnovolov.forgetmenot.presentation.screen.settings

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsEvent.*

class SettingsController(
    private val navigator: Navigator,
    private val fullscreenPreference: FullscreenPreference,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<SettingsEvent, Nothing>() {
    override fun handle(event: SettingsEvent) {
        when (event) {
            WalkingModeSettingsButtonClicked -> {
                navigator.navigateToWalkingModeSettingsFromNavHost()
            }

            FullscreenInExerciseCheckboxClicked -> {
                with(fullscreenPreference) {
                    isEnabledInExercise = !isEnabledInExercise
                }
            }

            FullscreenInRepetitionCheckboxClicked -> {
                with(fullscreenPreference) {
                    isEnabledInCardPlayer = !isEnabledInCardPlayer
                }
            }

            FullscreenInOtherPlacesCheckboxClicked -> {
                with(fullscreenPreference) {
                    isEnabledInOtherPlaces = !isEnabledInOtherPlaces
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}