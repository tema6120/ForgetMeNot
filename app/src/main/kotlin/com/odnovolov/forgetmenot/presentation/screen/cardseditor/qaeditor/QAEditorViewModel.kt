package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.EditableCardLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class QAEditorViewModel(
    private val cardId: Long,
    cardsEditor: CardsEditor
) {
    private val editableCard: Flow<EditableCard> =
        cardsEditor.state.flowOf(CardsEditor.State::editableCards)
            .map { editableCards: List<EditableCard> ->
                editableCards.find { editableCard: EditableCard -> editableCard.card.id == cardId }
            }
            .filterNotNull()

    val question: Flow<String> = editableCard.flatMapLatest { editableCard: EditableCard ->
        editableCard.flowOf(EditableCard::question)
    }

    val answer: Flow<String> = editableCard.flatMapLatest { editableCard: EditableCard ->
        editableCard.flowOf(EditableCard::answer)
    }

    val isLearned: Flow<Boolean> = editableCard.flatMapLatest { editableCard: EditableCard ->
        editableCard.flowOf(EditableCard::isLearned)
    }

    val label: Flow<EditableCardLabel?> = when (cardsEditor) {
        is CardsEditorForEditingDeck -> {
            combine(
                question,
                answer
            ) { question: String, answer: String ->
                if (question.isEmpty() && answer.isEmpty()) {
                    null
                } else {
                    val isDuplicated = cardsEditor.state.editableCards
                        .any { otherEditableCard: EditableCard ->
                            if (otherEditableCard.card.id == cardId) return@any false
                            otherEditableCard.question.trim() == question.trim()
                                    && otherEditableCard.answer.trim() == answer.trim()
                        }
                    if (isDuplicated) {
                        EditableCardLabel.DUPLICATED
                    } else {
                        null
                    }
                }
            }
        }
        is CardsEditorForEditingSpecificCards -> {
            when {
                cardsEditor.exercise != null -> {
                    editableCard.map { editableCard: EditableCard ->
                        when {
                            editableCard.card.isFullyBlank() -> EditableCardLabel.NEW
                            editableCard.card.isCurrentInExercise(cardsEditor.exercise) ->
                                EditableCardLabel.CURRENT_IN_EXERCISE
                            else -> EditableCardLabel.FOUND
                        }
                    }
                }
                cardsEditor.player != null -> {
                    editableCard.map { editableCard: EditableCard ->
                        when {
                            editableCard.card.isFullyBlank() -> EditableCardLabel.NEW
                            editableCard.card.isCurrentInPlayer(cardsEditor.player) ->
                                EditableCardLabel.CURRENT_IN_PLAYER
                            else -> EditableCardLabel.FOUND
                        }
                    }
                }
                else -> flowOf(null)
            }
        }
        else -> flowOf(null)
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    private fun Card.isFullyBlank(): Boolean = question.isBlank() && answer.isBlank()

    private fun Card.isCurrentInExercise(exercise: Exercise): Boolean {
        val currentCardInExercise: Card = with(exercise.state) {
            exerciseCards[currentPosition].base.card
        }
        return this.id == currentCardInExercise.id
    }

    private fun Card.isCurrentInPlayer(player: Player): Boolean {
        val currentCardInExercise: Card = with(player.state) {
            playingCards[currentPosition].card
        }
        return this.id == currentCardInExercise.id
    }
}