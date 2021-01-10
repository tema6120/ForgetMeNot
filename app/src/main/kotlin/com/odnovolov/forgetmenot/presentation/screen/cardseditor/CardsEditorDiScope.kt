package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.persistence.shortterm.CardsEditorProvider
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorViewModel

class CardsEditorDiScope private constructor(
    initialCardsEditor: CardsEditor? = null
) {
    private val cardsEditorProvider: ShortTermStateProvider<CardsEditor> = CardsEditorProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val cardsEditor: CardsEditor =
        initialCardsEditor ?: cardsEditorProvider.load()

    val controller = CardsEditorController(
        cardsEditor,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        cardsEditorProvider
    )

    val viewModel = CardsEditorViewModel(
        cardsEditor
    )

    fun qaEditorController(cardId: Long) = QAEditorController(
        cardId,
        cardsEditor,
        AppDiScope.get().longTermStateSaver,
        cardsEditorProvider
    )

    fun qaEditorViewModel(cardId: Long) = QAEditorViewModel(
        cardId,
        cardsEditor.state
    )

    companion object : DiScopeManager<CardsEditorDiScope>() {
        fun create(cardsEditor: CardsEditor) =
            CardsEditorDiScope(cardsEditor)

        override fun recreateDiScope() = CardsEditorDiScope()

        override fun onCloseDiScope(diScope: CardsEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}