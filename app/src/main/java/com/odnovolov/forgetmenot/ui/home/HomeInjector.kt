package com.odnovolov.forgetmenot.ui.home

import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.db.AppDatabase
import com.odnovolov.forgetmenot.common.GlobalInjector

object HomeInjector {

    fun viewModel(fragment: HomeFragment): HomeViewModel {
        val context = fragment.requireContext()
        val db = AppDatabase.getInstance(context.applicationContext)
        val sharedPrefs = GlobalInjector.sharedPreferences(context)
        val repository = HomeRepository(db, sharedPrefs)
        val factory = HomeViewModelImpl.Factory(repository)
        return ViewModelProviders.of(fragment, factory).get(HomeViewModelImpl::class.java)
    }

}