package com.odnovolov.forgetmenot.ui.exercisecreator

import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.common.GlobalInjector

object ExerciseCreatorInjector {

    fun viewModel(fragment: ExerciseCreatorFragment): ExerciseCreatorViewModel {
        val context = fragment.requireContext()
        val db = GlobalInjector.db(context)
        val dao = db.exerciseCreatorDao()
        val factory = ExerciseCreatorViewModelImpl.Factory(dao)
        val viewModelProvider = ViewModelProviders.of(fragment, factory)
        return viewModelProvider.get(ExerciseCreatorViewModelImpl::class.java)
    }

}