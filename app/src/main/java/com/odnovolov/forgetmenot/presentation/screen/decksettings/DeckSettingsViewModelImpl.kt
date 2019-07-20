package com.odnovolov.forgetmenot.presentation.screen.decksettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.odnovolov.forgetmenot.presentation.common.ActionSender
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.*
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.Action.ShowRenameDeckDialog
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.Event.RandomOrderSwitcherClicked
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.Event.RenameDeckButtonClicked

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

    private val actionSender = ActionSender<Action>()

    override val action = actionSender.getAction()

    override fun onEvent(event: Event) {
        when (event) {
            RenameDeckButtonClicked -> {
                actionSender.send(ShowRenameDeckDialog(deckId))
            }
            RandomOrderSwitcherClicked -> {
                val updatedRandomOrder = state.randomOrder.value?.not() ?: return
                dao.updateRandomOrder(updatedRandomOrder, deckId)
            }
        }
    }

}