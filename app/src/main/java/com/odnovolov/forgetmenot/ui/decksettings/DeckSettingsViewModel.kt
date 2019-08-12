package com.odnovolov.forgetmenot.ui.decksettings

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.common.ViewModel
import com.odnovolov.forgetmenot.entity.Pronunciation
import com.odnovolov.forgetmenot.ui.decksettings.DeckSettingsViewModel.*

interface DeckSettingsViewModel : ViewModel<State, Action, Event> {

    data class State(
        val deckName: LiveData<String>,
        val randomOrder: LiveData<Boolean>,
        val pronunciation: LiveData<Pronunciation>
    )

    sealed class Action {
        data class ShowRenameDeckDialog(val deckId: Int) : Action()
        data class NavigateToPronunciation(val initPronunciation: Pronunciation?) : Action()
    }

    sealed class Event {
        object RenameDeckButtonClicked : Event()
        object RandomOrderSwitcherClicked : Event()
        object PronunciationButtonClicked : Event()
        data class GotPronunciation(val resultPronunciation: Pronunciation) : Event()
    }

}