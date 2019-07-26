package com.odnovolov.forgetmenot.ui.home

import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.common.GlobalInjector
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModelImpl

object HomeInjector {

    fun viewModel(fragment: HomeFragment): HomeViewModel {
        val context = fragment.requireContext()
        val db = GlobalInjector.db(context)
        val sharedPrefs = GlobalInjector.sharedPreferences(context)
        val repository = HomeRepository(db, sharedPrefs)
        val exerciseViewModel = ExerciseCreatorViewModelImpl(db.exerciseCreatorDao())
        val factory = HomeViewModelImpl.Factory(
            fragment,
            repository,
            db.addDeckDao(),
            exerciseViewModel
        )
        return ViewModelProviders.of(fragment, factory).get(HomeViewModelImpl::class.java)
    }

}