package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder.Event.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.SetDialogText
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.ShowErrorMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.InputStream

class AddDeckController(
    private val addDeckScreenState: AddDeckScreenState,
    private val deckAdder: DeckAdder,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val addDeckStateProvider: ShortTermStateProvider<DeckAdder.State>,
    private val addDeckScreenStateProvider: ShortTermStateProvider<AddDeckScreenState>
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val commandFlow = EventFlow<AddDeckCommand>()
    val commands: Flow<AddDeckCommand> = commandFlow.get()

    init {
        deckAdder.events
            .onEach { event: DeckAdder.Event ->
                when (event) {
                    is ParsingFinishedWithError -> {
                        commandFlow.send(ShowErrorMessage(event.exception))
                    }
                    is DeckNameIsOccupied -> {
                        commandFlow.send(SetDialogText(event.occupiedName))
                    }
                    is DeckHasBeenAdded -> {
                        val deckSettingsState = DeckSettings.State(event.deck)
                        navigator.navigateToDeckSettings(deckSettingsState)
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    fun onContentReceived(inputStream: InputStream, fileName: String?) {
        deckAdder.addFrom(inputStream, fileName)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onDialogTextChanged(dialogText: String) {
        addDeckScreenState.typedText = dialogText
    }

    fun onPositiveDialogButtonClicked() {
        deckAdder.proposeDeckName(addDeckScreenState.typedText)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onNegativeDialogButtonClicked() {
        deckAdder.cancel()
        longTermStateSaver.saveStateByRegistry()
    }

    fun performSaving() {
        addDeckStateProvider.save(deckAdder.state)
        addDeckScreenStateProvider.save(addDeckScreenState)
    }

    fun onCleared() {
        coroutineScope.cancel()
    }
}