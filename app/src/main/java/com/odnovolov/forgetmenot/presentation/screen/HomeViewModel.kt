package com.odnovolov.forgetmenot.presentation.screen

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.presentation.common.LifecycleAwareViewModel
import com.odnovolov.forgetmenot.presentation.di.Injector

class HomeViewModel : LifecycleAwareViewModel() {

    val deck: LiveData<Deck?>
    val isRenameDialogVisible: LiveData<Boolean?>
    val isProcessing: LiveData<Boolean?>

    init {
        val component = Injector.getHomeViewModelComponent()

        val liveDataProvider = component.provideLiveDataProvider()
        deck = liveDataProvider.deck
        isRenameDialogVisible = liveDataProvider.isRenameDialogVisible
        isProcessing = liveDataProvider.isProcessing

        val binding = component.provideBinding()
        binding.setup(this)
    }
}