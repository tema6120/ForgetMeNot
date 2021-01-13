package com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RenameDeckViewModel(
    private val deckEditorState: DeckEditor.State,
    dialogState: RenameDeckDialogState,
    private val globalState: GlobalState
) {
    val deckName: String get() = deckEditorState.deck.name

    val deckNameCheckResult: Flow<NameCheckResult> =
        dialogState.flowOf(RenameDeckDialogState::typedDeckName)
            .map { typedDeckName: String -> checkDeckName(typedDeckName, globalState) }
}