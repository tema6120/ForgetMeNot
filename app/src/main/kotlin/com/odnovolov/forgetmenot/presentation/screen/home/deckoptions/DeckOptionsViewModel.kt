package com.odnovolov.forgetmenot.presentation.screen.home.deckoptions

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class DeckOptionsViewModel(
    homeScreenState: HomeScreenState,
    globalState: GlobalState
) {
    private val deckLists: Flow<List<DeckList>> = combine(
        globalState.flowOf(GlobalState::deckLists),
        homeScreenState.flowOf(HomeScreenState::deckForDeckOptionMenu)
    ) { deckLists: Collection<DeckList>, deckForDeckOptionMenu: Deck? ->
        val deck = deckForDeckOptionMenu ?: return@combine emptyList()
        deckLists.mapNotNull { deckList: DeckList ->
            if (deck.id in deckList.deckIds) deckList else null
        }
    }
        .share()

    val deckListIndicatorColors: Flow<List<Int>> =
        deckLists.map { deckLists: List<DeckList> ->
            deckLists.map { deckList: DeckList -> deckList.color }
        }

    val deckName: Flow<String?> =
        homeScreenState.flowOf(HomeScreenState::deckForDeckOptionMenu)
            .map { deck: Deck? -> deck?.name }

    val isDeckPinned: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::deckForDeckOptionMenu)
            .map { deck: Deck? -> deck?.isPinned ?: false }

    val namesOfDeckListsToWhichDeckBelongs: Flow<List<String>> =
        deckLists.map { deckLists: List<DeckList> ->
            deckLists.map { deckList: DeckList -> deckList.name }
        }
}