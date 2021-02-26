package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.ExistingDeck
import com.odnovolov.forgetmenot.domain.entity.NewDeck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToImportCards
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportScreenState
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileController.Command
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileController.Command.AskToUseSelectedDeckForImportNextFiles
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToRenameNewDeckForFileImport
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CardsFileController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val screenState: FileImportScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val fileImporterStateProvider: ShortTermStateProvider<FileImporter.State>,
    private val screenStateProvider: ShortTermStateProvider<FileImportScreenState>
) : BaseController<CardsFileEvent, Command>() {
    sealed class Command {
        class AskToUseSelectedDeckForImportNextFiles(
            val nameOfSelectedDeck: String,
            val countOfNextFiles: Int
        ) : Command()
    }

    override fun handle(event: CardsFileEvent) {
        when (event) {
            RenameDeckButtonClicked -> {
                val abstractDeck: AbstractDeck = with(fileImporter.state) {
                    files[currentPosition].deckWhereToAdd
                }
                if (abstractDeck is NewDeck) {
                    navigator.showRenameDeckDialogFromFileImport {
                        val dialogState = RenameDeckDialogState(
                            purpose = ToRenameNewDeckForFileImport,
                            typedDeckName = abstractDeck.deckName
                        )
                        RenameDeckDiScope.create(dialogState)
                    }
                }
            }

            AddCardsToNewDeckButtonClicked -> {
                navigator.showRenameDeckDialogFromFileImport {
                    val state = RenameDeckDialogState(purpose = ToRenameNewDeckForFileImport)
                    RenameDeckDiScope.create(state)
                }
            }

            is SubmittedNameForNewDeck -> {
                val newDeck = NewDeck(event.deckName)
                fileImporter.setDeckWhereToAdd(newDeck)
            }

            AddCardsToExistingDeckButtonClicked -> {
                navigator.navigateToDeckChooserFromFileImport {
                    val screenState = DeckChooserScreenState(purpose = ToImportCards)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is TargetDeckIsSelected -> {
                val deck = ExistingDeck(event.deck)
                fileImporter.setDeckWhereToAdd(deck)
                if (fileImporter.state.maxVisitedPosition < fileImporter.state.files.lastIndex
                    && fileImporter.state.currentPosition == fileImporter.state.maxVisitedPosition
                    && !screenState.wasAskedToUseSelectedDeckForImportNextFiles
                ) {
                    val nameOfSelectedDeck: String = event.deck.name
                    val countOfNextFiles: Int =
                        fileImporter.state.files.lastIndex - fileImporter.state.currentPosition
                    val command = AskToUseSelectedDeckForImportNextFiles(
                        nameOfSelectedDeck,
                        countOfNextFiles
                    )
                    coroutineScope.launch {
                        delay(300)
                        sendCommand(command)
                    }
                    sendCommand(command)
                    screenState.wasAskedToUseSelectedDeckForImportNextFiles = true
                }
            }

            UserAcceptedToUseSelectedDeckForImportNextFiles -> {
                fileImporter.useCurrentDeckForNextFiles()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(fileImporter.state)
        screenStateProvider.save(screenState)
    }
}