package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionCreatorStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionSettingsScreenStateProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repetitionSettingsModule = module {
    scope<RepetitionSettings> {
        scoped { RepetitionSettings(globalState = get()) }
        scoped { RepetitionCreatorStateProvider(globalState = get()) }
        scoped { get<RepetitionCreatorStateProvider>().load() }
        scoped { RepetitionStateCreator(state = get(), globalState = get()) }
        scoped { RepetitionSettingsScreenStateProvider() }
        scoped { get<RepetitionSettingsScreenStateProvider>().load() }
        scoped {
            RepetitionSettingsController(
                repetitionSettings = get(),
                repetitionStateCreator = get(),
                screenState = get(),
                globalState = get(),
                navigator = get(),
                longTermStateSaver = get(),
                repetitionCreatorStateProvider = get<RepetitionCreatorStateProvider>(),
                screenStateProvider = get<RepetitionSettingsScreenStateProvider>()
            )
        }
        viewModel {
            RepetitionSettingsViewModel(
                screenState = get(),
                repetitionStateCreator = get(),
                globalState = get()
            )
        }
    }
}

const val REPETITION_SETTINGS_SCOPE_ID = "REPETITION_SETTINGS_SCOPE_ID"