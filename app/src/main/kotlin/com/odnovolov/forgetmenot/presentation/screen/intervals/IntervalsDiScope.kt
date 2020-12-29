package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.persistence.shortterm.PresetDialogStateProvider
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope

class IntervalsDiScope {
    val intervalsSettings = IntervalsSettings(
        DeckSettingsDiScope.get()!!.deckSettings
    )

    val controller = IntervalsController(
        DeckSettingsDiScope.get()!!.deckSettings.state,
        intervalsSettings,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver
    )

    val viewModel = IntervalsViewModel(
        DeckSettingsDiScope.get()!!.deckSettings.state
    )

    val adapter = IntervalAdapter(controller)

    companion object : DiScopeManager<IntervalsDiScope>() {
        override fun recreateDiScope() = IntervalsDiScope()

        override fun onCloseDiScope(diScope: IntervalsDiScope) {
            diScope.controller.dispose()
        }
    }
}