package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetViewModel
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val intervalsModule = module {
    scope<IntervalsViewModel> {
        scoped {
            IntervalsSettings(
                deckSettings = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                globalState = get()
            )
        }
        scoped { PresetDialogStateProvider(serializableId = "IntervalScheme Preset State") }
        scoped { get<PresetDialogStateProvider>().load() }
        scoped<SkeletalPresetController> {
            IntervalsPresetController(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                intervalsSettings = get(),
                presetDialogState = get(),
                globalState = get(),
                longTermStateSaver = get(),
                presetDialogStateProvider = get<PresetDialogStateProvider>()
            )
        }
        scoped<SkeletalPresetViewModel> {
            IntervalsPresetViewModel(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                presetDialogState = get(),
                globalState = get()
            )
        }
        scoped {
            IntervalsController(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                intervalsSettings = get(),
                longTermStateSaver = get()
            )
        }
        viewModel {
            IntervalsViewModel(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get()
            )
        }
    }
}

const val INTERVALS_SCOPE_ID = "INTERVALS_SCOPE_ID"