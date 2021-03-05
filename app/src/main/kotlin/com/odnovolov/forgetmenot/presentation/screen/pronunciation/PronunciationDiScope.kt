package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PronunciationScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class PronunciationDiScope private constructor(
    initialScreenState: PronunciationScreenState? = null
) {
    private val screenStateProvider = PronunciationScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: PronunciationScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val pronunciationSettings = PronunciationSettings(
        DeckSettingsDiScope.getOrRecreate().deckSettings
    )

    private val audioFocusManager = AudioFocusManager(
        AppDiScope.get().app
    )

    private val pronunciationPreferences = PronunciationPreferences(emptySet()) // todo

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow,
        audioFocusManager
    )

    val controller = PronunciationController(
        pronunciationSettings,
        screenState,
        pronunciationPreferences,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = PronunciationViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        speakerImpl,
        screenState,
        pronunciationPreferences
    )

    companion object : DiScopeManager<PronunciationDiScope>() {
        fun create(initialScreenState: PronunciationScreenState) =
            PronunciationDiScope(initialScreenState)

        override fun recreateDiScope() = PronunciationDiScope()

        override fun onCloseDiScope(diScope: PronunciationDiScope) {
            diScope.audioFocusManager.abandonAllRequests()
            diScope.controller.dispose()
            diScope.speakerImpl.shutdown()
        }
    }
}