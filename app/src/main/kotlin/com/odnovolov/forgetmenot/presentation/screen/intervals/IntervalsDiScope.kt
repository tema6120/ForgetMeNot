package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.persistence.shortterm.IntervalsScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class IntervalsDiScope private constructor(
    initialScreenState: IntervalsScreenState? = null
) {
    private val screenStateProvider = IntervalsScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: IntervalsScreenState =
        initialScreenState ?: screenStateProvider.load()

    val intervalsSettings = IntervalsSettings(
        DeckSettingsDiScope.getOrRecreate().deckSettings
    )

    val controller = IntervalsController(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        intervalsSettings,
        screenState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = IntervalsViewModel(
        DeckSettingsDiScope.getOrRecreate().deckSettings.state,
        screenState
    )

    val adapter = IntervalAdapter(controller)

    companion object : DiScopeManager<IntervalsDiScope>() {
        fun create(initialScreenState: IntervalsScreenState) =
            IntervalsDiScope(initialScreenState)

        override fun recreateDiScope() = IntervalsDiScope()

        override fun onCloseDiScope(diScope: IntervalsDiScope) {
            diScope.controller.dispose()
        }
    }
}