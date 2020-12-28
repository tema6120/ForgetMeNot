package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.interactor.decksettings.PronunciationSettings
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class PronunciationDiScope {
    private val pronunciationSettings = PronunciationSettings(
        DeckSettingsDiScope.shareDeckSettings()
    )

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow
    )

    val controller = PronunciationController(
        pronunciationSettings,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = PronunciationViewModel(
        DeckSettingsDiScope.shareDeckSettings().state,
        speakerImpl
    )

    companion object : DiScopeManager<PronunciationDiScope>() {
        override fun recreateDiScope() = PronunciationDiScope()

        override fun onCloseDiScope(diScope: PronunciationDiScope) {
            diScope.controller.dispose()
            diScope.speakerImpl.shutdown()
        }
    }
}