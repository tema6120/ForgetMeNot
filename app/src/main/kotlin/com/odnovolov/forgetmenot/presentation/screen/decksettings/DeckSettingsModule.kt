package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsStateProvider
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val deckSettingsModule = module {
    scope<DeckSettingsViewModel> {
        scoped { DeckSettingsStateProvider(globalState = get()) }
        scoped { get<DeckSettingsStateProvider>().load() }
        scoped { DeckSettingsScreenStateProvider() }
        scoped { get<DeckSettingsScreenStateProvider>().load() }
        scoped { DeckSettings(state = get(), globalState = get()) }
        scoped {
            DeckSettingsController(
                deckSettingsScreenState = get(),
                deckSettings = get(),
                globalState = get(),
                navigator = get(),
                longTermStateSaver = get(),
                deckSettingsStateProvider = get<DeckSettingsStateProvider>(),
                deckSettingsScreenStateProvider = get<DeckSettingsScreenStateProvider>()
            )
        }
        viewModel {
            DeckSettingsViewModel(
                deckSettingsScreenState = get(),
                deckSettingsState = get(),
                globalState = get()
            )
        }
    }
}

const val DECK_SETTINGS_SCOPED_ID = "DECK_SETTINGS_SCOPED_ID"