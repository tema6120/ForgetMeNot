package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.persistence.serializablestate.RepetitionSettingsStateProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val repetitionSettingsModule = module {
    scope<RepetitionSettings> {
        scoped { RepetitionSettingsStateProvider(globalState = get()) }
        scoped { get<RepetitionSettingsStateProvider>().load() }
        scoped { RepetitionSettings(state = get()) }
        scoped {
            RepetitionSettingsController(
                repetitionSettings = get(),
                navigator = get(),
                repetitionSettingsStateProvider = get<RepetitionSettingsStateProvider>()
            )
        } onClose { it?.onCleared() }
        viewModel { RepetitionSettingsViewModel(repetitionSettingsState = get()) }
    }
}

const val REPETITION_SETTINGS_SCOPE_ID = "REPETITION_SETTINGS_SCOPE_ID"