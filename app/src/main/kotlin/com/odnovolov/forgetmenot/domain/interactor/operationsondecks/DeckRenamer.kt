package com.odnovolov.forgetmenot.domain.interactor.deckeditor

import com.odnovolov.forgetmenot.domain.entity.*

fun renameDeck(
    newName: String,
    deck: Deck,
    globalState: GlobalState
): Boolean {
    return if (checkDeckName(newName, globalState) == NameCheckResult.Ok) {
        deck.name = newName
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