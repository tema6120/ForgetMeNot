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
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
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
            HelpImportFileButtonClicked -> {
                navigator.navigateToHelpArticleFromNavHost {
                    val screenState = HelpArticleScreenState(HelpArticle.ImportOfDeck)
                    HelpArticleDiScope.create(screenState)
                }
            }

            AddCardsHereButtonClicked -> {
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
                        navigateToDeckEditor(result.deck)
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
                            navigateToDeckEditor(result.deck)
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
                        navigator.navigateToCardsEditorFromNavHost {
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

    private fun navigateToDeckEditor(deck: Deck) {
        navigator.navigateToDeckEditorFromNavHost {
            val deckEditorState = DeckEditor.State(deck)
            DeckEditorDiScope.create(
                DeckEditorScreenState(deck, DeckEditorScreenTab.Settings),
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