package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.flow.*

class QAEditorViewModel(
    private val cardId: Long,
    cardsEditorState: CardsEditor.State
) {
    private val editableCard: Flow<EditableCard?> =
        cardsEditorState.flowOf(CardsEditor.State::editableCards)
            .map { editableCards: List<EditableCard> ->
                editableCards.find { editableCard: EditableCard -> editableCard.card.id == cardId }
            }

    val question: Flow<String> = editableCard.flatMapLatest { editableCard: EditableCard? ->
        editableCard?.flowOf(EditableCard::question) ?: flowOf(null)
    }
        .filterNotNull()

    val answer: Flow<String> = editableCard.flatMapLatest { editableCard: EditableCard? ->
        editableCard?.flowOf(EditableCard::answer) ?: flowOf(null)
    }
        .filterNotNull()


    val isLearned: Flow<Boolean> = editableCard.flatMapLatest { editableCard: EditableCard? ->
        editableCard?.flowOf(EditableCard::isLearned) ?: flowOf(null)
    }
        .filterNotNull()
}