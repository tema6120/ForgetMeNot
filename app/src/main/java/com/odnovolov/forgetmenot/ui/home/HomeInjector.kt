package com.odnovolov.forgetmenot.ui.home

import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.common.GlobalInjector

object HomeInjector {

    fun viewModel(fragment: HomeFragment): HomeViewModel {
        val context = fragment.requireContext()
        val db = GlobalInjector.db(context)
        val sharedPrefs = GlobalInjector.sharedPreferences(context)
        val addDeckDao = db.addDeckDao()
        val repository = HomeRepository(db, sharedPrefs)
        val factory = HomeViewModelImpl.Factory(fragment, addDeckDao, repository)
        return ViewModelProviders.of(fragment, factory).get(HomeViewModelImpl::class.java)
    }

}