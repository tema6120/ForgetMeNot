package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckChooserScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference

class DeckChooserDiScope private constructor(
    initialScreenState: DeckChooserScreenState? = null
) {
    private val deckReviewPreference: DeckReviewPreference =
        DeckReviewPreferenceProvider(AppDiScope.get().database).load()

    private val screenStateProvider = DeckChooserScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: DeckChooserScreenState =
        initialScreenState ?: screenStateProvider.load()

    val controller = DeckChooserController(
        deckReviewPreference,
        screenState,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        screenStateProvider
    )

    val viewModel = DeckChooserViewModel(
        screenState,
        AppDiScope.get().globalState,
        deckReviewPreference
    )

    companion object : DiScopeManager<DeckChooserDiScope>() {
        fun create(screenState: DeckChooserScreenState) = DeckChooserDiScope(screenState)

        override fun recreateDiScope() = DeckChooserDiScope()

        override fun onCloseDiScope(diScope: DeckChooserDiScope) {
            diScope.controller.dispose()
        }
    }
}