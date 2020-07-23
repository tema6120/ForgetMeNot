package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.flow.*

class CardsEditorViewModel(
    private val cardsEditor: CardsEditor
) {
    val cardIds: Flow<List<Long>> = cardsEditor.state.flowOf(CardsEditor.State::editableCards)
        .map { editableCards: List<EditableCard> ->
            editableCards.map { it.card.id }
        }

    val currentPosition: Int get() = cardsEditor.state.currentPosition

    private val currentEditableCard: Flow<EditableCard?> = combine(
        cardsEditor.state.flowOf(CardsEditor.State::editableCards),
        cardsEditor.state.flowOf(CardsEditor.State::currentPosition)
    ) { editableCards: List<EditableCard>, currentPosition: Int ->
        if (currentPosition !in 0..editableCards.lastIndex) null
        else editableCards[currentPosition]
    }
        .share()

    val levelOfKnowledgeForCurrentCard: Flow<Int?> =
        currentEditableCard.flatMapLatest { editableCard: EditableCard? ->
            editableCard?.flowOf(EditableCard::levelOfKnowledge) ?: flowOf(null)
        }

    val isCurrentEditableCardLearned: Flow<Boolean?> =
        currentEditableCard.flatMapLatest { editableCard: EditableCard? ->
            editableCard?.flowOf(EditableCard::isLearned) ?: flowOf(null)
        }

    val isCurrentCardRemovable: Flow<Boolean> = combine(
        cardsEditor.state.flowOf(CardsEditor.State::editableCards),
        cardsEditor.state.flowOf(CardsEditor.State::currentPosition)
    ) { _, _ -> cardsEditor.isCurrentCardRemovable() }
}