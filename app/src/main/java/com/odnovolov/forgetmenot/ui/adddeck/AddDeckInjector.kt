package com.odnovolov.forgetmenot.ui.adddeck

import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.common.GlobalInjector

object AddDeckInjector {

    fun viewModel(fragment: AddDeckFragment): AddDeckViewModel {
        val context = fragment.requireContext()
        val db = GlobalInjector.db(context)
        val dao = db.addDeckDao()
        val factory = AddDeckViewModelImpl.Factory(fragment, dao)
        val viewModelProvider = ViewModelProviders.of(fragment, factory)
        return viewModelProvider.get(AddDeckViewModelImpl::class.java)
    }

}