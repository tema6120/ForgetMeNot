package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import com.odnovolov.forgetmenot.domain.architecturecomponents.copyableListOf
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName

fun createDeck(deckName: String, globalState: GlobalState): CardsEditorForEditingDeck? {
    return when (checkDeckName(deckName, globalState)) {
        NameCheckResult.Ok -> {
            val newDeck = Deck(id = generateId(), name = deckName, cards = copyableListOf())
            globalState.decks = (globalState.decks + newDeck).toCopyableList()
            val initialEditableCard = EditableCard(
                Card(id = generateId(), question = "", answer = ""),
                newDeck
            )
            val cardsEditorState = CardsEditor.State(editableCards = listOf(initialEditableCard))
            CardsEditorForEditingDeck(
                newDeck,
                isNewDeck = true,
                cardsEditorState,
                globalState
            )
        }
        else -> null
    }
}