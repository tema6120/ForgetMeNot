package com.odnovolov.forgetmenot.presentation.common

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState

interface Store {
    fun loadGlobalState(): GlobalState
    fun loadDeckReviewPreference(): DeckReviewPreference
    fun saveStateByRegistry()

    fun loadHomeScreenState(): HomeScreenState
    fun save(homeScreenState: HomeScreenState)
    fun deleteHomeScreenState()

    fun loadAddDeckState(): AddDeck.State
    fun saveAddDeckState(addDeckState: AddDeck.State)
    fun deleteAddDeckState()

    fun loadAddDeckScreenState(): AddDeckScreenState
    fun saveAddDeckScreenState(addDeckScreenState: AddDeckScreenState)
    fun deleteAddDeckScreenState()
}