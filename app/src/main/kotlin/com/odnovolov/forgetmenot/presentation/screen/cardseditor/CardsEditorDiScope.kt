package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.persistence.shortterm.CardsEditorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorViewModel

class CardsEditorDiScope private constructor(
    initialCardsEditorState: CardsEditor.State? = null
) {
    private val cardsEditorStateProvider = CardsEditorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val cardsEditorState: CardsEditor.State =
        initialCardsEditorState ?: cardsEditorStateProvider.load()

    private val cardsEditor = CardsEditor(
        cardsEditorState,
        AppDiScope.get().globalState
    )

    val controller = CardsEditorController(
        cardsEditor,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        cardsEditorStateProvider
    )

    val viewModel = CardsEditorViewModel(
        cardsEditorState
    )

    fun qaEditorController(cardId: Long) = QAEditorControllerImpl(
        cardId,
        cardsEditor,
        AppDiScope.get().longTermStateSaver
    )

    fun qaEditorViewModel(cardId: Long): QAEditorViewModel {
        val editableCard = cardsEditorState.editableCards.first { it.card.id == cardId }
        return QAEditorViewModel(editableCard)
    }

    companion object : DiScopeManager<CardsEditorDiScope>() {
        fun create(initialCardsEditorState: CardsEditor.State) =
            CardsEditorDiScope(initialCardsEditorState)

        override fun recreateDiScope() = CardsEditorDiScope()

        override fun onCloseDiScope(diScope: CardsEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}