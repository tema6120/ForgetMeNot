package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck
import com.odnovolov.forgetmenot.persistence.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.globalstate.provision.GlobalStateProvider
import com.odnovolov.forgetmenot.persistence.serializablestate.AddDeckScreenStateProvider
import com.odnovolov.forgetmenot.persistence.serializablestate.AddDeckStateProvider
import com.odnovolov.forgetmenot.persistence.serializablestate.HomeScreenStateProvider
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class StoreImpl : Store, CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Job() + dbDispatcher

    private inline fun background(crossinline block: () -> Unit) {
        launch {
            database.transaction {
                block()
            }
        }
    }

    override fun loadGlobalState(): GlobalState = GlobalStateProvider.load()

    override fun loadDeckReviewPreference(): DeckReviewPreference =
        DeckReviewPreferenceProvider.load()

    override fun saveStateByRegistry() {
        val changes = PropertyChangeRegistry.removeAll()
        background { changes.forEach(ChangeSaver::save) }
    }

    override fun loadHomeScreenState(): HomeScreenState = HomeScreenStateProvider.load()

    override fun save(homeScreenState: HomeScreenState) =
        background { HomeScreenStateProvider.save(homeScreenState) }

    override fun deleteHomeScreenState() = background { HomeScreenStateProvider.delete() }

    override fun loadAddDeckState(): AddDeck.State = AddDeckStateProvider.load()

    override fun saveAddDeckState(addDeckState: AddDeck.State) =
        background { AddDeckStateProvider.save(addDeckState) }

    override fun deleteAddDeckState() = background { AddDeckStateProvider.delete() }

    override fun loadAddDeckScreenState(): AddDeckScreenState = AddDeckScreenStateProvider.load()

    override fun saveAddDeckScreenState(addDeckScreenState: AddDeckScreenState) =
        background { AddDeckScreenStateProvider.save(addDeckScreenState) }

    override fun deleteAddDeckScreenState() = background { AddDeckScreenStateProvider.delete() }
}