package com.odnovolov.forgetmenot.ui.decksettings

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.odnovolov.forgetmenot.common.GlobalInjector

object DeckSettingsInjector {

    fun viewModel(fragment: DeckSettingsFragment): DeckSettingsViewModel {
        val context = fragment.requireContext()
        val db = GlobalInjector.db(context)
        val dao = db.deckSettingsDao()
        val deckId = fragment.navArgs<DeckSettingsFragmentArgs>().value.deckId
        val factory = DeckSettingsViewModelImpl.Factory(dao, deckId)
        val viewModelProvider = ViewModelProviders.of(fragment, factory)
        return viewModelProvider.get(DeckSettingsViewModelImpl::class.java)
    }

}