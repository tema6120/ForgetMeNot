package com.odnovolov.forgetmenot.presentation.screen.home

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import com.odnovolov.forgetmenot.presentation.common.LifecycleAwareViewModel
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.di.viewmodelscope.HomeViewModelComponent

class HomeViewModel : LifecycleAwareViewModel() {

    val decksPreview: LiveData<List<DeckPreview>?>
    val isRenameDialogVisible: LiveData<Boolean?>
    val isProcessing: LiveData<Boolean?>

    private val component: HomeViewModelComponent = Injector.createHomeViewModelComponent()

    init {
        val liveDataProvider = component.provideLiveDataProvider()
        decksPreview = liveDataProvider.decksPreview
        isRenameDialogVisible = liveDataProvider.isRenameDialogVisible
        isProcessing = liveDataProvider.isProcessing

        val binding = component.provideBinding()
        binding.setup(this)
    }
}