package com.odnovolov.forgetmenot.domain.interactor.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.InvalidNameException
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.Ok
import com.odnovolov.forgetmenot.domain.entity.checkDeckName

class DeckEditor(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(val deck: Deck)

    fun renameDeck(newName: String) {
        when (val nameCheckResult = checkDeckName(newName, globalState)) {
            Ok -> state.deck.name = newName
            else -> throw InvalidNameException(nameCheckResult)
        }
    }
}