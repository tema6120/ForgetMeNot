package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor.Event.*
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream

class AddDeckController(
    private val addDeckScreenState: AddDeckScreenState,
    private val addDeckInteractor: AddDeckInteractor,
    private val store: Store
) {
    val commands: Flow<AddDeckCommand> = addDeckInteractor.events.map { event: AddDeckInteractor.Event ->
        when (event) {
            is ParsingFinishedWithError -> ShowErrorMessage(event.exception)
            is DeckNameIsOccupied -> SetDialogText(event.occupiedName)
            is DeckHasBeenAdded -> {
                // todo: prepare DeckSettings state
                NavigateToDeckSettings
            }
        }
    }

    fun onContentReceived(inputStream: InputStream, fileName: String?) {
        addDeckInteractor.addFrom(inputStream, fileName)
        store.saveStateByRegistry()
    }

    fun onDialogTextChanged(dialogText: CharSequence?) {
        addDeckScreenState.typedText = dialogText?.toString() ?: ""
    }

    fun onPositiveDialogButtonClicked() {
        addDeckInteractor.proposeDeckName(addDeckScreenState.typedText)
        store.saveStateByRegistry()
    }

    fun onNegativeDialogButtonClicked() {
        addDeckInteractor.cancel()
        store.saveStateByRegistry()
    }

    fun onViewModelCleared() {
        store.save(addDeckScreenState)
        store.save(addDeckInteractor.state)
    }
}