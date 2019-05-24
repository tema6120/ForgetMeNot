package com.odnovolov.forgetmenot.presentation.screen

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.presentation.common.LifecycleAwareViewModel
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.di.viewmodelscope.HomeViewModelComponent

class HomeViewModel : LifecycleAwareViewModel() {

    val deckNames: LiveData<List<String>?>
    val isRenameDialogVisible: LiveData<Boolean?>
    val isProcessing: LiveData<Boolean?>

    private val component: HomeViewModelComponent = Injector.createHomeViewModelComponent()

    init {
        val liveDataProvider = component.provideLiveDataProvider()
        deckNames = liveDataProvider.deckNames
        isRenameDialogVisible = liveDataProvider.isRenameDialogVisible
        isProcessing = liveDataProvider.isProcessing

        val binding = component.provideBinding()
        binding.setup(this)
    }
}