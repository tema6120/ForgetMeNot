package com.odnovolov.forgetmenot.presentation.screen.home.choosedecklist

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.home.*
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToAddDeckToDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToRemoveDeckFromDeckList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ChooseDeckListViewModel(
    homeScreenState: HomeScreenState,
    private val globalState: GlobalState,
    private val deckReviewPreference: DeckReviewPreference
) {
    val purpose: Flow<ChooseDeckListDialogPurpose?> =
        homeScreenState.flowOf(HomeScreenState::chooseDeckListDialogPurpose)

    val selectableDeckLists: Flow<List<SelectableDeckList>> = combine(
        homeScreenState.flowOf(HomeScreenState::deckSelection),
        homeScreenState.flowOf(HomeScreenState::deckForDeckOptionMenu),
        globalState.flowOf(GlobalState::deckLists),
        purpose
    ) { deckSelection: DeckSelection?,
        deckForDeckOptionMenu: Deck?,
        deckLists: Collection<DeckList>,
        purpose: ChooseDeckListDialogPurpose?
        ->
        val relevantDeckIds: List<Long> = homeScreenState.deckSelection?.selectedDeckIds
            ?: homeScreenState.deckForDeckOptionMenu?.let { listOf(it.id) }
            ?: return@combine emptyList()
        val filteredDeckLists: List<DeckList> = when (purpose) {
            ToAddDeckToDeckList -> {
                globalState.deckLists.filter { deckList: DeckList ->
                    relevantDeckIds.any { deckId: Long -> deckId !in deckList.deckIds }
                }
            }
            ToRemoveDeckFromDeckList -> {
                globalState.deckLists.filter { deckList: DeckList ->
                    relevantDeckIds.any { deckId: Long -> deckId in deckList.deckIds }
                }
            }
            null -> {
                return@combine emptyList()
            }
        }
        val sortedDeckLists: List<DeckList> = filteredDeckLists.sortedBy { it.name }
        val currentDeckList = deckReviewPreference.currentDeckList
        sortedDeckLists.map { deckList: DeckList ->
            SelectableDeckList(
                deckList.id,
                deckList.name,
                deckList.color,
                deckList.deckIds.size,
                isSelected = deckList.id == currentDeckList?.id
            )
        }
    }
}