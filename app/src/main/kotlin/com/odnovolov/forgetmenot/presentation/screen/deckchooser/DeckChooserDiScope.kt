package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.persistence.longterm.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.shortterm.DeckChooserScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference

class DeckChooserDiScope private constructor(
    initialScreenState: DeckChooserScreenState? = null
) {
    private val screenStateProvider = DeckChooserScreenStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database
    )

    private val screenState: DeckChooserScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val deckReviewPreference: DeckReviewPreference = run {
        val deckReviewPreferenceId: Long = when (screenState.purpose) {
            ToImportCards -> DeckReviewPreference.ID_TO_IMPORT_CARDS
            ToMergeInto -> DeckReviewPreference.ID_TO_MERGE
            ToMoveCard, ToMoveCardsInDeckEditor, ToMoveCardsInSearch, ToMoveCardsInHomeSearch ->
                DeckReviewPreference.ID_TO_MOVE
            ToCopyCard, ToCopyCardsInDeckEditor, ToCopyCardsInSearch, ToCopyCardsInHomeSearch ->
                DeckReviewPreference.ID_TO_COPY
        }
        DeckReviewPreferenceProvider(
            deckReviewPreferenceId,
            AppDiScope.get().database,
            AppDiScope.get().globalState
        ).load()
    }

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