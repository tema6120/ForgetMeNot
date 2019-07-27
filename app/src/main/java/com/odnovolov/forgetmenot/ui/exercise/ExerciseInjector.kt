package com.odnovolov.forgetmenot.ui.exercise

import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.common.GlobalInjector

object ExerciseInjector {

    fun viewModel(fragment: ExerciseFragment): ExerciseViewModel {
        val context = fragment.requireContext()
        val db = GlobalInjector.db(context)
        val dao = db.exerciseDao()
        val factory = ExerciseViewModelImpl.Factory(dao)
        val viewModelProvider = ViewModelProviders.of(fragment, factory)
        return viewModelProvider.get(ExerciseViewModelImpl::class.java)
    }
}