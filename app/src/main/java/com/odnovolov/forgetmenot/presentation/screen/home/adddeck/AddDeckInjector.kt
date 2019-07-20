package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import com.odnovolov.forgetmenot.data.db.AppDatabase

object AddDeckInjector {

    fun viewModel(fragment: AddDeckFragment): AddDeckViewModel {
        val dao = dao(fragment.requireContext())
        val factory = AddDeckViewModelImpl.Factory(fragment, dao)
        return ViewModelProviders.of(fragment, factory).get(AddDeckViewModelImpl::class.java)
    }

    private fun dao(context: Context): AddDeckDao {
        return AppDatabase.getInstance(context.applicationContext)
            .addDeckDao()
    }

}