package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class QAEditorViewModel(
    private val cardId: Long,
    cardsEditor: CardsEditor
) {
    private val editableCard: Flow<EditableCard?> =
        cardsEditor.state.flowOf(CardsEditor.State::editableCards)
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

    val isDuplicated: Flow<Boolean> =
        if (cardsEditor !is CardsEditorForEditingDeck) {
            flowOf(false)
        } else {
            combine(
                question,
                answer
            ) { question: String, answer: String ->
                if (question.isEmpty() && answer.isEmpty()) {
                    false
                } else {
                    cardsEditor.state.editableCards.any { otherEditableCard: EditableCard ->
                        if (otherEditableCard.card.id == cardId) return@any false
                        otherEditableCard.question.trim() == question.trim()
                                && otherEditableCard.answer.trim() == answer.trim()
                    }
                }
            }
                .flowOn(Dispatchers.Default)
        }
}