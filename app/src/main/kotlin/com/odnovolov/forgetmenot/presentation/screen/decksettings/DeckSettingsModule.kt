package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.Store
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val deckSettingsModule = module {
    scope<DeckSettingsViewModel> {
        scoped { get<Store>().loadDeckSettingsState(globalState = get()) }
        scoped { get<Store>().loadDeckSettingsScreenState() }
        scoped { DeckSettings(state = get(), globalState = get()) }
        scoped {
            DeckSettingsController(
                deckSettingsScreenState = get(),
                deckSettings = get(),
                globalState = get(),
                navigator = get(),
                store = get()
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