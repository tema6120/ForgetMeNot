package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder.Event.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsScreenState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.SetDialogText
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckCommand.ShowErrorMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.java.KoinJavaComponent.getKoin
import java.io.InputStream

class AddDeckController(
    private val addDeckScreenState: AddDeckScreenState,
    private val deckAdder: DeckAdder,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val addDeckStateProvider: UserSessionTermStateProvider<DeckAdder.State>,
    private val addDeckScreenStateProvider: UserSessionTermStateProvider<AddDeckScreenState>
) {
    private val coroutineScope = MainScope()
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
                        val koinScope =
                            getKoin().createScope<DeckSettingsViewModel>(DECK_SETTINGS_SCOPED_ID)
                        koinScope.declare(deckSettingsState, override = true)
                        koinScope.declare(DeckSettingsScreenState(), override = true)
                        navigator.navigateToDeckSettings()
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    fun onContentReceived(inputStream: InputStream, fileName: String?) {
        deckAdder.addFrom(inputStream, fileName)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onDialogTextChanged(dialogText: CharSequence?) {
        addDeckScreenState.typedText = dialogText?.toString() ?: ""
    }

    fun onPositiveDialogButtonClicked() {
        deckAdder.proposeDeckName(addDeckScreenState.typedText)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onNegativeDialogButtonClicked() {
        deckAdder.cancel()
        longTermStateSaver.saveStateByRegistry()
    }

    fun onFragmentPause() {
        addDeckStateProvider.save(deckAdder.state)
        addDeckScreenStateProvider.save(addDeckScreenState)
    }

    fun onCleared() {
        coroutineScope.cancel()
    }
}