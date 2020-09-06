package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsDiScope

class PronunciationDiScope private constructor(
    initialPresetDialogState: PresetDialogState? = null
) {
    private val presetDialogStateProvider = PresetDialogStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = "Pronunciation Preset State"
    )

    private val presetDialogState: PresetDialogState =
        initialPresetDialogState ?: presetDialogStateProvider.load()

    private val pronunciationScreenState = PronunciationScreenState()

    private val pronunciationSettings = PronunciationSettings(
        DeckSettingsDiScope.shareDeckSettings(),
        AppDiScope.get().globalState
    )

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow
    )

    val presetController = PronunciationPresetController(
        DeckSettingsDiScope.shareDeckSettings().state,
        pronunciationSettings,
        presetDialogState,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        presetDialogStateProvider
    )

    val presetViewModel = PronunciationPresetViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        presetDialogState,
        AppDiScope.get().globalState
    )

    val controller = PronunciationController(
        pronunciationSettings,
        DeckSettingsDiScope.shareDeckSettings().state,
        pronunciationScreenState,
        speakerImpl,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = PronunciationViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        pronunciationScreenState,
        speakerImpl
    )

    companion object : DiScopeManager<PronunciationDiScope>() {
        fun create(initialPresetDialogState: PresetDialogState) =
            PronunciationDiScope(initialPresetDialogState)

        override fun recreateDiScope() = PronunciationDiScope()

        override fun onCloseDiScope(diScope: PronunciationDiScope) {
            diScope.controller.dispose()
            diScope.presetController.dispose()
            diScope.speakerImpl.shutdown()
        }
    }
}