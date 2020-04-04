package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionCreatorStateProvider
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repetitionSettingsModule = module {
    scope<RepetitionSettings> {
        scoped { RepetitionSettings(globalState = get()) }
        scoped { RepetitionCreatorStateProvider(globalState = get()) }
        scoped { get<RepetitionCreatorStateProvider>().load() }
        scoped { RepetitionStateCreator(state = get(), globalState = get()) }
        scoped { PresetDialogStateProvider(serializableId = "RepetitionSetting Preset State") }
        scoped { get<PresetDialogStateProvider>().load() }
        scoped<SkeletalPresetController> {
            RepetitionSettingsPresetController(
                repetitionSettings = get(),
                presetDialogState = get(),
                globalState = get(),
                longTermStateSaver = get(),
                presetDialogStateProvider = get<PresetDialogStateProvider>()
            )
        }
        scoped<SkeletalPresetViewModel> {
            RepetitionSettingsPresetViewModel(
                presetDialogState = get(),
                globalState = get()
            )
        }
        scoped {
            RepetitionSettingsController(
                repetitionSettings = get(),
                repetitionStateCreator = get(),
                globalState = get(),
                navigator = get(),
                longTermStateSaver = get(),
                repetitionCreatorStateProvider = get<RepetitionCreatorStateProvider>()
            )
        }
        viewModel {
            RepetitionSettingsViewModel(
                repetitionStateCreator = get(),
                globalState = get()
            )
        }
    }
}

const val REPETITION_SETTINGS_SCOPE_ID = "REPETITION_SETTINGS_SCOPE_ID"