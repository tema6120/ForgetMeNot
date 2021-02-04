package com.odnovolov.forgetmenot.domain.interactor.deckeditor

import com.odnovolov.forgetmenot.domain.entity.*

fun renameDeck(
    newName: String,
    abstractDeck: AbstractDeck,
    globalState: GlobalState
): Boolean {
    return if (checkDeckName(newName, globalState) == NameCheckResult.Ok) {
        when(abstractDeck) {
            is NewDeck -> abstractDeck.deckName = newName
            is ExistingDeck -> abstractDeck.deck.name = newName
            else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
        }
        true
    } else {
        false
    }
}

fun checkDeckName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.decks.any { it.name == testingName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}