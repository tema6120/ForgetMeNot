package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck
import com.odnovolov.forgetmenot.presentation.common.Store
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

class AddDeckController(
    private val addDeckScreenState: AddDeckScreenState,
    private val addDeck: AddDeck,
    private val store: Store
) {
    private val commandFlow = EventFlow<AddDeckCommand>()
    val commands: Flow<AddDeckCommand> = commandFlow.get()

    fun onContentReceived(inputStream: InputStream, fileName: String?) {
        addDeck.addFrom(inputStream, fileName)
        store.saveStateByRegistry()
    }

    fun onDialogTextChanged(dialogText: CharSequence?) {
        addDeckScreenState.typedText = dialogText?.toString() ?: ""
    }

    fun onPositiveDialogButtonClicked() {
        addDeck.proposeDeckName(addDeckScreenState.typedText)
        store.saveStateByRegistry()
    }

    fun onNegativeDialogButtonClicked() {
        addDeck.cancel()
        store.saveStateByRegistry()
    }
}