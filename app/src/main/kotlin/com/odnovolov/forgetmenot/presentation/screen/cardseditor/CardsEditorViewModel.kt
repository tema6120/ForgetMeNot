package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardsEditorViewModel(
    private val cardsEditorState: CardsEditor.State
) {
    val cardIds: Flow<List<Long>> = cardsEditorState.flowOf(CardsEditor.State::editableCards)
        .map { editableCards: List<EditableCard> ->
            editableCards.map { it.card.id }
        }

    val currentPosition: Int get() = cardsEditorState.currentPosition
}