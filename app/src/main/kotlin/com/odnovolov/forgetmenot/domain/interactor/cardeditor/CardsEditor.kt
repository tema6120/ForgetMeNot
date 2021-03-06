package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.copyableListOf
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId

abstract class CardsEditor(
    val state: State,
    protected val globalState: GlobalState
) {
    class State(
        editableCards: List<EditableCard>,
        currentPosition: Int = 0,
        removals: MutableList<CardRemoving> = ArrayList(),
        movements: MutableList<CardMoving> = ArrayList(),
        copyOperations: MutableList<CardCopying> = ArrayList(),
        createdDecks: MutableList<Deck> = ArrayList()
    ) : FlowMaker<State>() {
        var editableCards: List<EditableCard> by flowMaker(editableCards)
        var currentPosition: Int by flowMaker(currentPosition)
        val removals: MutableList<CardRemoving> by flowMaker(removals)
        val movements: MutableList<CardMoving> by flowMaker(movements)
        val copyOperations: MutableList<CardCopying> by flowMaker(copyOperations)
        val createdDecks: MutableList<Deck> by flowMaker(createdDecks)
    }

    protected val currentEditableCard: EditableCard
        get() = state.editableCards[state.currentPosition]

    protected fun isPositionValid(): Boolean =
        state.currentPosition in 0..state.editableCards.lastIndex

    fun isCurrentCardMovable(): Boolean =
        isPositionValid() && !currentEditableCard.hasBlankField()

    fun setCurrentPosition(position: Int) {
        if (!isPositionValid()) return
        state.currentPosition = position
    }

    open fun setQuestion(question: String) {
        if (!isPositionValid()) return
        currentEditableCard.question = question
    }

    open fun setAnswer(answer: String) {
        if (!isPositionValid()) return
        currentEditableCard.answer = answer
    }

    open fun setIsLearned(isLearned: Boolean) {
        if (!isPositionValid()) return
        currentEditableCard.isLearned = isLearned
    }

    open fun setGrade(grade: Int) {
        if (!isPositionValid()) return
        currentEditableCard.grade = grade
    }

    fun removeCard(): Boolean {
        if (!isPositionValid()) return false
        if (!isCurrentCardRemovable()) return false
        with(state) {
            editableCards = editableCards.toMutableList().apply {
                val movedCard = removeAt(currentPosition)
                removals.add(CardRemoving(movedCard, currentPosition))
            }
            currentPosition = when {
                editableCards.isEmpty() -> -1
                currentPosition > editableCards.lastIndex -> editableCards.lastIndex
                else -> currentPosition
            }
        }
        return true
    }

    fun restoreLastRemovedCard() {
        with(state) {
            if (removals.isEmpty()) return
            val lastCardRemoving: CardRemoving = removals.removeLast()
            val lastRemovedCard: EditableCard = lastCardRemoving.editableCard
            val insertPosition: Int = minOf(lastCardRemoving.positionInSource, editableCards.size)
            editableCards = editableCards.toMutableList().apply {
                add(insertPosition, lastRemovedCard)
            }
            currentPosition = insertPosition
        }
    }

    fun copyTo(abstractDeck: AbstractDeck): Boolean {
        if (!isPositionValid()) return false
        if (!isCurrentCardMovable()) return false
        val deck: Deck = getOrCreateDeckFrom(abstractDeck)
        val cardCopying = CardCopying(
            copyOfQuestion = currentEditableCard.question,
            copyOfAnswer = currentEditableCard.answer,
            targetDeck = deck
        )
        state.copyOperations.add(cardCopying)
        return true
    }

    fun cancelLastCopying() {
        with(state) {
            if (copyOperations.isEmpty()) return
            copyOperations.removeLast()
        }
    }

    protected fun applyCopying() {
        state.copyOperations.groupBy(
            keySelector = { cardCopying: CardCopying -> cardCopying.targetDeck },
            valueTransform = { cardCopying: CardCopying ->
                Card(
                    id = generateId(),
                    question = cardCopying.copyOfQuestion,
                    answer = cardCopying.copyOfAnswer
                )
            }
        ).forEach { (deckToCopyTo: Deck, copyCards: List<Card>) ->
            deckToCopyTo.cards = (deckToCopyTo.cards + copyCards).toCopyableList()
        }
    }

    protected fun getOrCreateDeckFrom(abstractDeck: AbstractDeck): Deck {
        return when (abstractDeck) {
            is ExistingDeck -> abstractDeck.deck
            is NewDeck -> {
                val sourceExercisePreference = currentEditableCard.deck.exercisePreference
                val exercisePreferenceForNewDeck =
                    if (sourceExercisePreference.isShared())
                        sourceExercisePreference else
                        ExercisePreference.Default
                val newDeck = Deck(
                    id = generateId(),
                    name = abstractDeck.deckName,
                    cards = copyableListOf(),
                    exercisePreference = exercisePreferenceForNewDeck
                )
                globalState.decks = (globalState.decks + newDeck).toCopyableList()
                state.createdDecks.add(newDeck)
                newDeck
            }
            else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
        }
    }

    abstract fun moveTo(abstractDeck: AbstractDeck): Boolean
    abstract fun cancelLastMovement()
    abstract fun isCurrentCardRemovable(): Boolean
    abstract fun areCardsEdited(): Boolean
    abstract fun save(): SavingResult

    fun finish() {
        val emptyCreatedDeckIds = state.createdDecks.mapNotNull { deck: Deck ->
            if (deck.cards.isEmpty()) deck.id else null
        }
        if (emptyCreatedDeckIds.isEmpty()) return
        globalState.decks = globalState.decks
            .filter { deck: Deck -> deck.id !in emptyCreatedDeckIds }
            .toCopyableList()
    }

    sealed class SavingResult {
        object Success : SavingResult()
        class Failure(val underfilledPositions: List<Int>) : SavingResult()
    }

    data class CardRemoving(
        val editableCard: EditableCard,
        val positionInSource: Int
    )

    data class CardMoving(
        val editableCard: EditableCard,
        val positionInSource: Int,
        val targetDeck: Deck
    )

    data class CardCopying(
        val copyOfQuestion: String,
        val copyOfAnswer: String,
        val targetDeck: Deck
    )
}