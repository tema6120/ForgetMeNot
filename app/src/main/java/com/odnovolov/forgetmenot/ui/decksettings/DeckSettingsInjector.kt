package com.odnovolov.forgetmenot.ui.decksettings

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.odnovolov.forgetmenot.db.AppDatabase

object DeckSettingsInjector {

    fun viewModel(
        fragment: DeckSettingsFragment
    ): DeckSettingsViewModel {
        val dao = deckSettingsDao(fragment.requireContext())
        val deckId = fragment.navArgs<DeckSettingsFragmentArgs>().value.deckId
        val factory = DeckSettingsViewModelImpl.Factory(dao, deckId)
        return ViewModelProviders.of(fragment, factory).get(DeckSettingsViewModelImpl::class.java)
    }

    private fun deckSettingsDao(context: Context): DeckSettingsDao {
        return AppDatabase.getInstance(context.applicationContext)
            .deckSettingsDao()
    }

}