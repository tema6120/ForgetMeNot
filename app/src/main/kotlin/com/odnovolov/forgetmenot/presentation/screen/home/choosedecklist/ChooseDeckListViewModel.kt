package com.odnovolov.forgetmenot.presentation.screen.home.choosedecklist

import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToAddDeckToDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.ChooseDeckListDialogPurpose.ToRemoveDeckFromDeckList
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.SelectableDeckList

class ChooseDeckListViewModel(
    private val homeScreenState: HomeScreenState,
    private val globalState: GlobalState,
    private val deckReviewPreference: DeckReviewPreference
) {
    private val relevantDeckIds: List<Long>
        get() {
            return homeScreenState.deckSelection?.selectedDeckIds
                ?: homeScreenState.deckForDeckOptionMenu?.let { listOf(it.id) }
                ?: emptyList()
        }

    val purpose: ChooseDeckListDialogPurpose
        get() = homeScreenState.chooseDeckListDialogPurpose!!

    val selectableDeckLists: List<SelectableDeckList>
        get() {
            val relevantDeckIds = relevantDeckIds
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
                    emptyList()
                }
            }
            val sortedDeckLists: List<DeckList> = filteredDeckLists.sortedBy { it.name }
            val currentDeckList = deckReviewPreference.currentDeckList
            return sortedDeckLists.map { deckList: DeckList ->
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