package com.odnovolov.forgetmenot.presentation.screen.decksettings

import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsScreenStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckSettingsStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*val deckSettingsModule = module {
    scope<DeckSettingsViewModel> {
        scoped { DeckSettingsStateProvider(globalState = get()) }
        scoped { get<DeckSettingsStateProvider>().load() }
        scoped { PresetDialogStateProvider(key = "ExercisePreference Preset State") }
        scoped { get<PresetDialogStateProvider>().load() }
        scoped { DeckSettingsScreenStateProvider() }
        scoped { get<DeckSettingsScreenStateProvider>().load() }
        scoped { DeckSettings(state = get(), globalState = get()) }
        scoped<SkeletalPresetController> {
            ExercisePreferencePresetController(
                deckSettings = get(),
                presetDialogState = get(),
                globalState = get(),
                presetDialogStateProvider = get<PresetDialogStateProvider>(),
                longTermStateSaver = get()
            )
        }
        scoped<SkeletalPresetViewModel> {
            ExercisePreferencePresetViewModel(
                presetDialogState = get(),
                deckSettingsState = get(),
                globalState = get()
            )
        }
        scoped {
            DeckSettingsController(
                deckSettingsScreenState = get(),
                deckSettings = get(),
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
}*/

const val DECK_SETTINGS_SCOPED_ID = "DECK_SETTINGS_SCOPED_ID"