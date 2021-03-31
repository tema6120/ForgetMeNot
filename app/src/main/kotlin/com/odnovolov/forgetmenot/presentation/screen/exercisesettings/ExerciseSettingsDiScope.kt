package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings.Companion.DEFAULT_CARD_FILTER_DISPLAY

class ExerciseSettingsDiScope {
    // TODO
    private val exerciseSettings = ExerciseSettings(cardPrefilterMode = DEFAULT_CARD_FILTER_DISPLAY)

    // TODO
    private val screenState = CardsThresholdDialogState("100")

    val controller = ExerciseSettingsController(
        exerciseSettings,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = ExerciseSettingsViewModel(
        exerciseSettings,
        screenState
    )

    companion object : DiScopeManager<ExerciseSettingsDiScope>() {
        override fun recreateDiScope() = ExerciseSettingsDiScope()

        override fun onCloseDiScope(diScope: ExerciseSettingsDiScope) {
            diScope.controller.dispose()
        }
    }
}