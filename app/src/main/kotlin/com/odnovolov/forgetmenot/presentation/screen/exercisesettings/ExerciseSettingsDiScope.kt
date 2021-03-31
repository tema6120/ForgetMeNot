package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.persistence.shortterm.CardsThresholdDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class ExerciseSettingsDiScope {
    private val dialogStateProvider = CardsThresholdDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val dialogState = try {
        dialogStateProvider.load()
    } catch (e: NoSuchElementException) {
        CardsThresholdDialogState()
    }

    val controller = ExerciseSettingsController(
        AppDiScope.get().exerciseSettings,
        dialogState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        dialogStateProvider
    )

    val viewModel = ExerciseSettingsViewModel(
        AppDiScope.get().exerciseSettings,
        dialogState
    )

    companion object : DiScopeManager<ExerciseSettingsDiScope>() {
        override fun recreateDiScope() = ExerciseSettingsDiScope()

        override fun onCloseDiScope(diScope: ExerciseSettingsDiScope) {
            diScope.controller.dispose()
        }
    }
}