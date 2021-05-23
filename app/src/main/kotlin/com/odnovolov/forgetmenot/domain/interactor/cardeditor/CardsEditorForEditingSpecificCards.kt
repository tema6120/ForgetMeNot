package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise

open class CardsEditorForEditingSpecificCards(
    state: State,
    globalState: GlobalState,
    val exercise: Exercise? = null,
    val player: Player? = null
) : CardsEditor(state, globalState) {
    init {
        ensureLastEmptyCard()
    }

    override fun setQuestion(question: String) {
        super.setQuestion(question)
        ensureLastEmptyCard()
    }

    override fun setAnswer(answer: String) {
        super.setAnswer(answer)
        ensureLastEmptyCard()
    }

    private fun ensureLastEmptyCard() {
        val deckWhereToAddNewCards: Deck = when {
            exercise != null -> {
                with(exercise.state) {
                    exerciseCards[currentPosition].base.deck
                }
            }
            player != null -> {
                with(player.state) {
                    playingCards[currentPosition].deck
                }
            }
            else -> return
        }
        with(state) {
            if (editableCards.last().isFullyBlank()) {
                var redundantCardCount = 0
                for (i in editableCards.lastIndex - 1 downTo currentPosition) {
                    if (editableCards[i].isFullyBlank()) {
                        redundantCardCount++
                    } else {
                        break
                    }
                }
                if (redundantCardCount > 0) {
                    editableCards = editableCards.dropLast(redundantCardCount)
                }
            } else {
                val newEditableCard = EditableCard(
                    Card(id = generateId(), question = "", answer = ""),
                    deckWhereToAddNewCards
                )
                editableCards = editableCards + newEditableCard
            }
        }
    }

    override fun isCurrentCardRemovable(): Boolean = isCurrentCardMovable()

    override fun moveTo(abstractDeck: AbstractDeck): Boolean {
        if (!isPositionValid()) return false
        if (!isCurrentCardMovable()) return false
        val deck: Deck = getOrCreateDeckFrom(abstractDeck)
        val cardMoving = CardMoving(currentEditableCard, state.currentPosition, deck)
        removeExistingCardMoving(cardMoving)
        state.movements.add(cardMoving)
        return true
    }

    private fun removeExistingCardMoving(cardMoving: CardMoving) {
        for (i in state.movements.lastIndex downTo 0) {
            val testedCardMoving = state.movements[i]
            if (testedCardMoving.editableCard.card.id == cardMoving.editableCard.card.id) {
                state.movements.removeAt(i)
            }
        }
    }

    override fun cancelLastMovement() {
        with(state) {
            if (movements.isEmpty()) return
            movements.removeLast()
        }
    }

    override fun areCardsEdited(): Boolean {
        if (state.removals.isNotEmpty()) return true
        if (state.movements.isNotEmpty()) return true
        if (state.copyOperations.isNotEmpty()) return true
        return state.editableCards.any { editableCard: EditableCard ->
            val originalCard = editableCard.card
            originalCard.question != editableCard.question
                    || originalCard.answer != editableCard.answer
                    || originalCard.isLearned != editableCard.isLearned
                    || originalCard.grade != editableCard.grade
        }
    }

    override fun save(): SavingResult {
        check()?.let { failure -> return failure }
        saveNewCards()
        applyCopying()
        applyRemovals()
        applyMovements()
        applyChanges()
        return Success
    }

    private fun check(): SavingResult.Failure? {
        val underfilledPositions: List<Int> =
            state.editableCards.mapIndexedNotNull { index, editableCard ->
                when {
                    editableCard.isNew() && editableCard.isHalfFilled() -> index
                    !editableCard.isNew() && editableCard.hasBlankField() -> index
                    else -> null
                }
            }
        return if (underfilledPositions.isEmpty()) null
        else SavingResult.Failure(underfilledPositions)
    }

    private fun saveNewCards() {
        for (editableCard in state.editableCards) {
            if (!editableCard.isNew()) continue
            if (editableCard.isFullyBlank()) continue
            editableCard.deck.cards = (editableCard.deck.cards + editableCard.card).toCopyableList()
        }
    }

    private fun applyRemovals() {
        with(state) {
            (removals.map { it.editableCard } + movements.map { it.editableCard })
                .groupBy(
                    keySelector = { editableCard: EditableCard -> editableCard.deck },
                    valueTransform = { editableCard: EditableCard -> editableCard.card.id }
                )
                .forEach { (deck: Deck, cardIdsToRemove: List<Long>) ->
                    deck.cards = deck.cards
                        .filter { card: Card -> card.id !in cardIdsToRemove }
                        .toCopyableList()
                }
            if (exercise != null || player != null) {
                val removedCardIds: List<Long> =
                    removals.map { cardRemoving: CardRemoving -> cardRemoving.editableCard.card.id }
                exercise?.notifyCardsRemoved(removedCardIds)
                player?.notifyCardsRemoved(removedCardIds)
            }
        }
    }

    private fun applyMovements() {
        with(state) {
            movements.groupBy(
                keySelector = { cardMoving: CardMoving -> cardMoving.targetDeck },
                valueTransform = { cardMoving: CardMoving ->
                    cardMoving.editableCard.card.apply {
                        question = cardMoving.editableCard.question
                        answer = cardMoving.editableCard.answer
                        isLearned = cardMoving.editableCard.isLearned
                        grade = cardMoving.editableCard.grade
                    }
                }
            ).forEach { (deckToMoveTo: Deck, movingCards: List<Card>) ->
                deckToMoveTo.cards = (deckToMoveTo.cards + movingCards).toCopyableList()
            }
            if (exercise != null || player != null) {
                val cardMovement: List<Exercise.CardMoving> =
                    movements.map { cardMoving: CardMoving ->
                        val cardId: Long = cardMoving.editableCard.card.id
                        val deckMoveTo: Deck = cardMoving.targetDeck
                        Exercise.CardMoving(cardId, deckMoveTo)
                    }
                exercise?.notifyCardsMoved(cardMovement)
                player?.notifyCardsMoved(cardMovement)
            }
        }
    }

    private fun applyChanges() {
        state.editableCards.forEach { editableCard: EditableCard ->
            val originalCard = editableCard.card
            var isQuestionChanged = false
            var isAnswerChanged = false
            var isGradeChanged = false
            var isIsLearnedChanged = false
            if (editableCard.question != originalCard.question) {
                originalCard.question = editableCard.question
                isQuestionChanged = true
            }
            if (editableCard.answer != originalCard.answer) {
                originalCard.answer = editableCard.answer
                isAnswerChanged = true
            }
            if (editableCard.grade != originalCard.grade) {
                originalCard.grade = editableCard.grade
                isGradeChanged = true
            }
            if (editableCard.isLearned != originalCard.isLearned) {
                originalCard.isLearned = editableCard.isLearned
                isIsLearnedChanged = true
            }
            if (exercise != null) {
                val isCardChanged: Boolean = isQuestionChanged || isAnswerChanged
                        || isGradeChanged || isIsLearnedChanged
                if (isCardChanged) {
                    exercise.notifyCardChanged(
                        originalCard.id,
                        isQuestionChanged,
                        isAnswerChanged,
                        isGradeChanged,
                        isIsLearnedChanged
                    )
                }
            }
        }
    }

    private fun EditableCard.isNew(): Boolean = card.question == "" && card.answer == ""
}