package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeckEditorViewModel(
    private val screenState: DeckEditorScreenState,
    deckEditorState: DeckEditor.State,
    private val globalState: GlobalState
) {
    val initialTab: DeckEditorScreenState.DeckEditorScreenTab
        get() = screenState.initialTab

    val deckName: Flow<String> = deckEditorState.deck.flowOf(Deck::name)

    val deckNameCheckResult: Flow<NameCheckResult> =
        screenState.flowOf(DeckEditorScreenState::typedDeckName)
            .map { typedDeckName: String -> checkDeckName(typedDeckName, globalState) }
}