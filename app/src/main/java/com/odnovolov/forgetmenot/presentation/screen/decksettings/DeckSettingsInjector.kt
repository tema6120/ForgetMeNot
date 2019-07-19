package com.odnovolov.forgetmenot.presentation.screen.decksettings

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.odnovolov.forgetmenot.data.db.AppDatabase
import com.odnovolov.forgetmenot.presentation.common.BaseViewModelFactory

object DeckSettingsInjector {

    fun viewModel(
        fragment: DeckSettingsFragment
    ): DeckSettingsViewModel {
        val deckId = fragment.navArgs<DeckSettingsFragmentArgs>().value.deckId
        val factory = viewModelFactory(fragment.requireContext(), deckId)
        return ViewModelProviders.of(fragment, factory).get(DeckSettingsViewModelImpl::class.java)
    }

    private fun viewModelFactory(
        context: Context,
        deckId: Int
    ): ViewModelProvider.Factory {
        return BaseViewModelFactory {
            DeckSettingsViewModelImpl(
                deckSettingsDao(context),
                deckId
            )
        }
    }

    private fun deckSettingsDao(context: Context): DeckSettingsDao {
        return AppDatabase.getInstance(context)
            .deckSettingsDao()
    }

}