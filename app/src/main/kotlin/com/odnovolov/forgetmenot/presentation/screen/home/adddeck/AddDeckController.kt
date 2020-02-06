package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeck.Event.*
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream

class AddDeckController(
    private val addDeckScreenState: AddDeckScreenState,
    private val addDeck: AddDeck,
    private val store: Store
) {
    val commands: Flow<AddDeckCommand> = addDeck.events.map { event: AddDeck.Event ->
        when (event) {
            is ParsingFinishedWithError -> ShowErrorMessage(event.exception)
            is DeckNameIsOccupied -> SetDialogText(event.occupiedName)
            is DeckHasAdded -> {
                // todo: prepare DeckSettings state
                NavigateToDeckSettings
            }
        }
    }

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