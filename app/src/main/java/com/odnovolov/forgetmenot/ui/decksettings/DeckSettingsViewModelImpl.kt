package com.odnovolov.forgetmenot.ui.decksettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.ui.decksettings.DeckSettingsViewModel.*
import com.odnovolov.forgetmenot.ui.decksettings.DeckSettingsViewModel.Action.ShowRenameDeckDialog
import com.odnovolov.forgetmenot.ui.decksettings.DeckSettingsViewModel.Event.RandomOrderSwitcherClicked
import com.odnovolov.forgetmenot.ui.decksettings.DeckSettingsViewModel.Event.RenameDeckButtonClicked
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class DeckSettingsViewModelImpl(
    private val dao: DeckSettingsDao,
    private val deckId: Int
) : ViewModel(), DeckSettingsViewModel {

    class Factory(val dao: DeckSettingsDao, val deckId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DeckSettingsViewModelImpl(dao, deckId) as T
        }
    }

    override val state = State(
        deckName = dao.getDeckName(deckId),
        randomOrder = dao.getRandomOrder(deckId)
    )

    private val actionSender = LiveEvent<Action>()
    override val action = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            RenameDeckButtonClicked -> {
                actionSender.send(ShowRenameDeckDialog(deckId))
            }
            RandomOrderSwitcherClicked -> {
                val updatedRandomOrder = state.randomOrder.value?.not() ?: return
                viewModelScope.launch(IO) {
                    dao.setRandomOrder(updatedRandomOrder, deckId)
                }
            }
        }
    }

}