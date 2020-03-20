package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.presentation.common.Store
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val repetitionSettingsModule = module {
    scope<RepetitionSettings> {
        scoped { get<Store>().loadRepetitionSettingsState(globalState = get()) }
        scoped { RepetitionSettings(state = get()) }
        scoped {
            RepetitionSettingsController(
                repetitionSettings = get(),
                navigator = get(),
                store = get()
            )
        } onClose { it?.onCleared() }
        viewModel { RepetitionSettingsViewModel(repetitionSettingsState = get()) }
    }
}

const val REPETITION_SETTINGS_SCOPE_ID = "REPETITION_SETTINGS_SCOPE_ID"