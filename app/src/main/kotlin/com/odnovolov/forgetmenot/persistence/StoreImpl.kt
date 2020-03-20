package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.deckreviewpreference.DeckReviewPreferenceProvider
import com.odnovolov.forgetmenot.persistence.globalstate.provision.GlobalStateProvider
import com.odnovolov.forgetmenot.persistence.walkingmodepreference.WalkingModePreferenceProvider
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
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

    override fun loadWalkingModePreference(): WalkingModePreference =
        WalkingModePreferenceProvider.load()

    override fun saveStateByRegistry() {
        val changes = PropertyChangeRegistry.removeAll()
        background { changes.forEach(ChangeSaver::save) }
    }
}