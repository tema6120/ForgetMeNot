package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId

class CardsEditorForDeckCreation(
    val deckName: String,
    private val globalState: GlobalState,
    state: State = State(listOf(EditableCard()))
) : CardsEditorForEditingDeck(state) {
    val createdDeck: Deck? get() = newDeck
    private var newDeck: Deck? = null

    override fun areCardsEdited(): Boolean = true

    override fun save(): SavingResult {
        checkDeck()?.let { failure -> return failure }
        val cards: CopyableList<Card> = applyChanges()
        newDeck = Deck(id = generateId(), name = deckName, cards = cards)
        globalState.decks = (globalState.decks + newDeck!!).toCopyableList()
        return SavingResult.Success
    }
}