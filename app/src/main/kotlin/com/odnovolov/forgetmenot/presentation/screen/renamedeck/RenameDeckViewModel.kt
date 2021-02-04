package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RenameDeckViewModel(
    private val dialogState: RenameDeckDialogState,
    private val globalState: GlobalState
) {
    val deckName: String get() = when(val abstractDeck = dialogState.abstractDeck) {
        is NewDeck -> abstractDeck.deckName
        is ExistingDeck -> abstractDeck.deck.name
        else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
    }

    val deckNameCheckResult: Flow<NameCheckResult> =
        dialogState.flowOf(RenameDeckDialogState::typedDeckName)
            .map { typedDeckName: String -> checkDeckName(typedDeckName, globalState) }
}