package com.odnovolov.forgetmenot.presentation.screen.binding

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.presentation.common.DucLiveData
import io.reactivex.functions.Consumer

class LiveDataProvider {

    val deckNames = DucLiveData<List<String>?>()
    val isRenameDialogVisible = DucLiveData<Boolean?>()
    val isProcessing = DucLiveData<Boolean?>()

    val stateConsumer = Consumer<AddNewDeckFeature.State> { state: AddNewDeckFeature.State ->
        deckNames.value = listOf("Phrasal Verbs", "Irregular Verbs", "My Vocabulary 21.05.19")
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
}