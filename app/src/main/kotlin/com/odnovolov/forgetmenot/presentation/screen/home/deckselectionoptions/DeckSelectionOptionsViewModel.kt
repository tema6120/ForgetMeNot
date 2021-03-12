package com.odnovolov.forgetmenot.presentation.screen.home.deckselectionoptions

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSelection
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DeckSelectionOptionsViewModel(
    screenState: HomeScreenState,
    globalState: GlobalState
) {
    private val selectedDecks: Flow<List<Deck>> = combine(
        screenState.flowOf(HomeScreenState::deckSelection),
        globalState.flowOf(GlobalState::decks)
    ) { deckSelection: DeckSelection?, decks: Collection<Deck> ->
        if (deckSelection == null) {
            emptyList()
        } else {
            decks.filter { deck: Deck -> deck.id in deckSelection.selectedDeckIds }
        }
    }
        .share()

    val numberOfSelectedDecks: Flow<Int> = selectedDecks.map { it.size }

    val canBePinned: Flow<Boolean> = selectedDecks.map { selectedDecks: List<Deck> ->
        selectedDecks.any { deck: Deck -> !deck.isPinned }
    }

    val canBeUnpinned: Flow<Boolean> = selectedDecks.map { selectedDecks: List<Deck> ->
        selectedDecks.any { deck: Deck -> deck.isPinned }
    }

    val namesOfDeckListsToWhichDecksBelong: Flow<List<String>> = combine(
        screenState.flowOf(HomeScreenState::deckSelection),
        globalState.flowOf(GlobalState::deckLists)
    ) { deckSelection: DeckSelection?, deckLists: Collection<DeckList> ->
        val selectedDeckIds = deckSelection?.selectedDeckIds ?: return@combine emptyList()
        deckLists.mapNotNull { deckList: DeckList ->
            val has = selectedDeckIds
                .any { selectedDeckId: Long -> selectedDeckId in deckList.deckIds }
            if (has) deckList.name else null
        }
    }
}