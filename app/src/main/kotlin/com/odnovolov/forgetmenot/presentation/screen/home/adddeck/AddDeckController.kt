package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder.Event.*
import com.odnovolov.forgetmenot.domain.interactor.deckadder.Parser.IllegalCardFormatException
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command.SetDialogText
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command.ShowErrorMessage
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckEvent.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AddDeckController(
    private val addDeckScreenState: AddDeckScreenState,
    private val deckAdder: DeckAdder,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val addDeckStateProvider: ShortTermStateProvider<DeckAdder.State>,
    private val addDeckScreenStateProvider: ShortTermStateProvider<AddDeckScreenState>
) : BaseController<AddDeckEvent, Command>() {
    sealed class Command {
        class ShowErrorMessage(val exception: IllegalCardFormatException) : Command()
        class SetDialogText(val text: String) : Command()
    }

    init {
        deckAdder.events
            .onEach { event: DeckAdder.Event ->
                when (event) {
                    is ParsingFinishedWithError -> {
                        sendCommand(ShowErrorMessage(event.exception))
                    }
                    is DeckNameIsOccupied -> {
                        sendCommand(SetDialogText(event.occupiedName))
                    }
                    is DeckHasBeenAdded -> {
                        navigator.navigateToDeckSetup(
                            createDeckSetupDiScope = {
                                val deckEditorState = DeckEditor.State(event.deck)
                                DeckSetupDiScope.create(DeckSetupScreenState(), deckEditorState)
                            },
                            createDeckSettingsDiScope = {
                                val deckSettingsState = DeckSettings.State(event.deck)
                                DeckSettingsDiScope.create(
                                    deckSettingsState,
                                    PresetDialogState()
                                )
                            }
                        )
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    override fun handle(event: AddDeckEvent) {
        when (event) {
            is ContentReceived -> {
                deckAdder.addFrom(event.inputStream, event.fileName)
            }

            is DialogTextChanged -> {
                addDeckScreenState.typedText = event.dialogText
            }

            PositiveDialogButtonClicked -> {
                deckAdder.proposeDeckName(addDeckScreenState.typedText)
            }

            NegativeDialogButtonClicked -> {
                deckAdder.cancel()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        addDeckStateProvider.save(deckAdder.state)
        addDeckScreenStateProvider.save(addDeckScreenState)
    }
}