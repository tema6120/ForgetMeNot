package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.createDeck
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.renameDeck
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.addDeckIds
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserEvent.SubmittedNewDeckName
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileEvent.SubmittedNameForNewDeck
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckController.Command
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckController.Command.ShowDeckHasBeenCreatedMessage
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckEvent.TextChanged

class RenameDeckController(
    private val dialogState: RenameDeckDialogState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val dialogStateProvider: ShortTermStateProvider<RenameDeckDialogState>
) : BaseController<RenameDeckEvent, Command>() {
    sealed class Command {
        class ShowDeckHasBeenCreatedMessage(val deckName: String) : Command()
    }

    override fun handle(event: RenameDeckEvent) {
        when (event) {
            is TextChanged -> {
                dialogState.typedDeckName = event.text
            }

            OkButtonClicked -> {
                val newName = dialogState.typedDeckName
                when (val purpose = dialogState.purpose) {
                    is ToRenameExistingDeck -> {
                        val success: Boolean = renameDeck(newName, purpose.deck, globalState)
                        if (success) navigator.navigateUp()
                    }
                    is ToRenameExistingDeckOnHomeScreen -> {
                        val success: Boolean = renameDeck(newName, purpose.deck, globalState)
                        if (success) {
                            HomeDiScope.getOrRecreate().screenState.updateDeckListSignal = Unit
                            navigator.navigateUp()
                        }
                    }
                    ToRenameNewDeckForFileImport -> {
                        if (checkDeckName(newName, globalState) == NameCheckResult.Ok) {
                            FileImportDiScope.getOrRecreate().cardsFileController
                                .dispatch(SubmittedNameForNewDeck(dialogState.typedDeckName))
                            navigator.navigateUp()
                        }
                    }
                    ToCreateNewDeck -> {
                        val cardsEditor: CardsEditorForEditingDeck? =
                            createDeck(newName, globalState)
                        if (cardsEditor != null) {
                            sendCommand(ShowDeckHasBeenCreatedMessage(cardsEditor.deck.name))
                            val deckListToView: DeckList? =
                                HomeDiScope.getOrRecreate().deckReviewPreference.deckList
                            deckListToView?.addDeckIds(listOf(cardsEditor.deck.id))
                            navigator.navigateToCardsEditorFromRenameDeckDialog {
                                CardsEditorDiScope.create(cardsEditor)
                            }
                        }
                    }
                    ToCreateNewForDeckChooser -> {
                        if (checkDeckName(newName, globalState) == NameCheckResult.Ok) {
                            DeckChooserDiScope.getOrRecreate().controller
                                .dispatch(SubmittedNewDeckName(dialogState.typedDeckName))
                            navigator.navigateUp()
                        }
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        dialogStateProvider.save(dialogState)
    }
}