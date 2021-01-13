package com.odnovolov.forgetmenot.domain.interactor.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.checkDeckName

class DeckEditor(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(val deck: Deck)

    fun renameDeck(newName: String): Boolean =
        if (checkDeckName(newName, globalState) == NameCheckResult.Ok) {
            state.deck.name = newName
            true
        } else {
            false
        }
}