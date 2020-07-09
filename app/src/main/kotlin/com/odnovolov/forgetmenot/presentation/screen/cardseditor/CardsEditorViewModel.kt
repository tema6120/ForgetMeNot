package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.flow.*

class CardsEditorViewModel(
    private val cardsEditorState: CardsEditor.State
) {
    val cardIds: Flow<List<Long>> = cardsEditorState.flowOf(CardsEditor.State::editableCards)
        .map { editableCards: List<EditableCard> ->
            editableCards.map { it.card.id }
        }

    val currentPosition: Int get() = cardsEditorState.currentPosition

    private val currentEditableCard: Flow<EditableCard> = combine(
        cardsEditorState.flowOf(CardsEditor.State::editableCards),
        cardsEditorState.flowOf(CardsEditor.State::currentPosition)
    ) { editableCards: List<EditableCard>, currentPosition: Int ->
        editableCards[currentPosition]
    }
        .distinctUntilChanged()
        .share()

    val levelOfKnowledgeForCurrentCard: Flow<Int> =
        currentEditableCard.flatMapLatest { editableCard: EditableCard ->
            editableCard.flowOf(EditableCard::levelOfKnowledge)
        }

    val isCurrentEditableCardLearned: Flow<Boolean> =
        currentEditableCard.flatMapLatest { editableCard: EditableCard ->
            editableCard.flowOf(EditableCard::isLearned)
        }

    val isRemoveButtonVisible: Flow<Boolean> = combine(
        cardsEditorState.flowOf(CardsEditor.State::editableCards),
        cardsEditorState.flowOf(CardsEditor.State::currentPosition)
    ) { editableCards: List<EditableCard>, currentPosition: Int ->
        currentPosition != editableCards.lastIndex
    }
}