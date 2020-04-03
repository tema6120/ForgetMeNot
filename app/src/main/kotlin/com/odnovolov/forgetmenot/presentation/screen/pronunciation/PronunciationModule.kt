package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetController
import com.odnovolov.forgetmenot.presentation.common.preset.SkeletalPresetViewModel
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.dsl.onClose

val pronunciationModule = module {
    scope<PronunciationViewModel> {
        scoped {
            PronunciationSettings(
                deckSettings = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                globalState = get()
            )
        }
        scoped { PresetDialogStateProvider(serializableId = "Pronunciation Preset State") }
        scoped { get<PresetDialogStateProvider>().load() }
        scoped<SkeletalPresetController> {
            PronunciationPresetController(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                pronunciationSettings = get(),
                presetDialogState = get(),
                globalState = get(),
                longTermStateSaver = get(),
                dialogStateProvider = get<PresetDialogStateProvider>()
            )
        }
        scoped<SkeletalPresetViewModel> {
            PronunciationPresetViewModel(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                presetDialogState = get(),
                globalState = get()
            )
        }
        scoped { SpeakerImpl(applicationContext = get()) } onClose { it?.shutdown() }
        scoped {
            PronunciationController(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                pronunciationSettings = get(),
                longTermStateSaver = get()
            )
        }
        viewModel {
            PronunciationViewModel(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get(),
                speakerImpl = get()
            )
        }
    }
}

const val PRONUNCIATION_SCOPE_ID = "PRONUNCIATION_SCOPE_ID"