package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.InvalidNameException
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckCreator
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.Failure
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.FailureCause.*
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.Success
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.Parser.IllegalCardFormatException
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command.SetDialogText
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckController.Command.ShowErrorMessage
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState.HowToAdd.CREATE
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState.HowToAdd.LOAD_FROM_FILE

class AddDeckController(
    private val screenState: AddDeckScreenState,
    private val deckCreator: DeckCreator,
    private val deckFromFileCreator: DeckFromFileCreator,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val deckFromFileCreatorStateProvider: ShortTermStateProvider<DeckFromFileCreator.State>,
    private val addDeckScreenStateProvider: ShortTermStateProvider<AddDeckScreenState>
) : BaseController<AddDeckEvent, Command>() {
    sealed class Command {
        class ShowErrorMessage(val exception: IllegalCardFormatException) : Command()
        class SetDialogText(val text: String) : Command()
    }

    override fun handle(event: AddDeckEvent) {
        when (event) {
            CreateDeckButtonClicked -> {
                screenState.howToAdd = CREATE
            }

            is ContentReceived -> {
                screenState.howToAdd = LOAD_FROM_FILE
                val result = deckFromFileCreator.loadFromFile(
                    event.inputStream,
                    event.fileName ?: ""
                )
                when (result) {
                    is Success -> {
                        navigateToDeckSetup(result.deck)
                        screenState.howToAdd = null
                    }
                    is Failure -> {
                        when (result.failureCause) {
                            is ParsingError -> {
                                sendCommand(ShowErrorMessage(result.failureCause.exception))
                                screenState.howToAdd = null
                            }
                            is DeckNameIsOccupied -> {
                                sendCommand(SetDialogText(result.failureCause.occupiedName))
                            }
                            DeckNameIsEmpty -> {
                            }
                        }
                    }
                }
            }

            is DialogTextChanged -> {
                screenState.typedText = event.dialogText
            }

            PositiveDialogButtonClicked -> {
                when (screenState.howToAdd) {
                    LOAD_FROM_FILE -> {
                        val result = deckFromFileCreator.proposeDeckName(screenState.typedText)
                        if (result is Success) {
                            navigateToDeckSetup(result.deck)
                            screenState.howToAdd = null
                        }
                    }
                    CREATE -> {
                        val cardsEditor: CardsEditor = try {
                            deckCreator.create(screenState.typedText)
                        } catch (e: InvalidNameException) {
                            // asynchronous behavior issue
                            return
                        }
                        navigator.navigateToCardsEditorFromHome {
                            CardsEditorDiScope.create(cardsEditor)
                        }
                        screenState.howToAdd = null
                    }
                }
            }

            NegativeDialogButtonClicked -> {
                deckFromFileCreator.cancel()
                screenState.howToAdd = null
            }
        }
    }

    private fun navigateToDeckSetup(deck: Deck) {
        navigator.navigateToDeckSetupFromHome {
            val deckEditorState = State(deck)
            DeckSetupDiScope.create(
                DeckSetupScreenState(deck),
                deckEditorState
            )
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        deckFromFileCreatorStateProvider.save(deckFromFileCreator.state)
        addDeckScreenStateProvider.save(screenState)
    }
}