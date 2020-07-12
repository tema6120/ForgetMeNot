package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.AllCardsAreEmpty
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.HasUnderfilledCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.Creation
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.EditingExistingDeck

class CardsEditor(
    val state: State,
    val globalState: GlobalState
) {
    class State (
        mode: Mode,
        editableCards: List<EditableCard> =
            when (mode) {
                is Creation -> listOf(EditableCard())
                is EditingExistingDeck -> mode.deck.cards.map(::EditableCard) + EditableCard()
            },
        currentPosition: Int = 0
    ) : FlowableState<State>() {
        val mode: Mode by me(mode)
        var editableCards: List<EditableCard> by me(editableCards)
        var currentPosition: Int by me(currentPosition)

        sealed class Mode {
            class Creation(val deckName: String) : Mode()
            class EditingExistingDeck(val deck: Deck) : Mode()
        }
    }

    val currentEditableCard: EditableCard get() = state.editableCards[state.currentPosition]
    private var cardBackup: CardBackup? = null

    fun setCurrentPosition(position: Int) {
        state.currentPosition = position
        ensureLastEmptyCard()
    }

    fun setQuestion(question: String) {
        currentEditableCard.question = question
        ensureLastEmptyCard()
    }

    fun setAnswer(answer: String) {
        currentEditableCard.answer = answer
        ensureLastEmptyCard()
    }

    private fun ensureLastEmptyCard() {
        with(state) {
            if (editableCards.last().isBlank()) {
                var redundantCardCount = 0
                for (i in editableCards.lastIndex - 1 downTo currentPosition) {
                    if (editableCards[i].isBlank()) {
                        redundantCardCount++
                    } else {
                        break
                    }
                }
                if (redundantCardCount > 0) {
                    editableCards = editableCards.dropLast(redundantCardCount)
                }
            } else {
                editableCards += EditableCard()
            }
        }
    }

    fun setIsLearned(isLearned: Boolean) {
        currentEditableCard.isLearned = isLearned
    }

    fun setLevelOfKnowledge(levelOfKnowledge: Int) {
        currentEditableCard.levelOfKnowledge = levelOfKnowledge
    }

    fun removeCard(): Boolean {
        with(state) {
            if (currentPosition == editableCards.lastIndex) return false
            cardBackup = CardBackup(editableCards[currentPosition], currentPosition)
            editableCards = editableCards.toMutableList().apply { removeAt(currentPosition) }
            return true
        }
    }

    fun restoreLastRemovedCard() {
        cardBackup?.let { cardBackup: CardBackup ->
            with(state) {
                // Because size of editableCards can be changed,
                // we correct insertPosition to avoid IndexOutOfBoundsException during inserting
                val insertPosition = minOf(cardBackup.position, editableCards.size)
                editableCards = editableCards.toMutableList().apply {
                    add(insertPosition, cardBackup.editableCard)
                }
            }
            ensureLastEmptyCard()
        }
        cardBackup = null
    }

    fun save(): SavingResult {
        return with(state) {
            when {
                editableCards.all(EditableCard::isBlank) -> SavingResult.Failure(AllCardsAreEmpty)
                editableCards.any(EditableCard::isUnderfilled) -> {
                    val positions: List<Int> = editableCards
                        .mapIndexedNotNull { index, editableCard ->
                            if (editableCard.isUnderfilled()) index else null
                        }
                    SavingResult.Failure(HasUnderfilledCards(positions))
                }
                else -> {
                    val cards: CopyableList<Card> = editableCards
                        .filterNot(EditableCard::isBlank)
                        .map { editableCard ->
                            editableCard.card.apply {
                                question = editableCard.question
                                answer = editableCard.answer
                                isLearned = editableCard.isLearned
                                levelOfKnowledge = editableCard.levelOfKnowledge
                            }
                        }
                        .toCopyableList()
                    val deckId: Long = when (val mode = mode) {
                        is Creation -> {
                            val deck = Deck(
                                id = generateId(),
                                name = mode.deckName,
                                cards = cards
                            )
                            globalState.decks = (globalState.decks + deck).toCopyableList()
                            deck.id
                        }
                        is EditingExistingDeck -> {
                            mode.deck.cards = cards
                            mode.deck.id
                        }
                    }
                    SavingResult.Success(deckId)
                }
            }
        }
    }

    sealed class SavingResult {
        class Success(val deckId: Long) : SavingResult()
        class Failure(val failureCause: FailureCause) : SavingResult()

        sealed class FailureCause {
            class HasUnderfilledCards(val positions: List<Int>) : FailureCause()
            object AllCardsAreEmpty : FailureCause()
        }
    }

    private class CardBackup(
        val editableCard: EditableCard,
        val position: Int
    )
}