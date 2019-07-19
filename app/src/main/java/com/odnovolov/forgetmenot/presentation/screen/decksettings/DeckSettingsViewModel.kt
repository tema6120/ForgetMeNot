package com.odnovolov.forgetmenot.presentation.screen.decksettings

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.presentation.common.ViewModel
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.*

interface DeckSettingsViewModel : ViewModel<State, Action, Event> {

    data class State(
        val deckName: LiveData<String>,
        val randomOrder: LiveData<Boolean>
    )

    sealed class Action {
        data class ShowRenameDeckDialog(val deckId: Int) : Action()
    }

    sealed class Event {
        object RenameDeckButtonClicked : Event()
        object RandomOrderSwitcherClicked : Event()
    }

}