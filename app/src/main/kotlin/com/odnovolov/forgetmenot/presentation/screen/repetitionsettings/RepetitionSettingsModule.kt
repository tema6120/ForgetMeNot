package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.persistence.usersessionterm.RepetitionSettingsStateProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repetitionSettingsModule = module {
    scope<RepetitionSettings> {
        scoped { RepetitionSettingsStateProvider(globalState = get()) }
        scoped { get<RepetitionSettingsStateProvider>().load() }
        scoped { RepetitionSettings(state = get()) }
        scoped {
            RepetitionSettingsController(
                repetitionSettings = get(),
                navigator = get(),
                longTermStateSaver = get(),
                repetitionSettingsStateProvider = get<RepetitionSettingsStateProvider>()
            )
        }
        viewModel { RepetitionSettingsViewModel(repetitionSettings = get()) }
    }
}

const val REPETITION_SETTINGS_SCOPE_ID = "REPETITION_SETTINGS_SCOPE_ID"