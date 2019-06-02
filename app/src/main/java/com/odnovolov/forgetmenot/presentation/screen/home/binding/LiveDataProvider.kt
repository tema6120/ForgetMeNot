package com.odnovolov.forgetmenot.presentation.screen.home.binding

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.common.DucLiveData
import io.reactivex.functions.Consumer

class LiveDataProvider {

    val decksPreview = DucLiveData<List<DeckPreview>?>()
    val isRenameDialogVisible = DucLiveData<Boolean?>()
    val isProcessing = DucLiveData<Boolean?>()

    val addNewDeckFeatureStateConsumer =
        Consumer<AddNewDeckFeature.State> { state: AddNewDeckFeature.State ->
            when (state.stage) {
                is Idle -> {
                    isRenameDialogVisible.value = false
                    isProcessing.value = false
                }
                is Processing, is Saving -> {
                    isRenameDialogVisible.value = false
                    isProcessing.value = true
                }
                is WaitingForName, is WaitingForChangingName -> {
                    isRenameDialogVisible.value = true
                    isProcessing.value = false
                }
            }
        }

    val decksPreviewFeatureStateConsumer =
        Consumer<DecksPreviewFeature.State> { state: DecksPreviewFeature.State ->
            decksPreview.value = state.decksPreview
        }
}