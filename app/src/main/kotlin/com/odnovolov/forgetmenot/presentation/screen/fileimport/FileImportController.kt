package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.ExistingDeck
import com.odnovolov.forgetmenot.domain.entity.NewDeck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToRenameNewDeckForFileImport
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState

class FileImportController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val fileImporterStateProvider: ShortTermStateProvider<FileImporter.State>
) : BaseController<FileImportEvent, Nothing>() {
    override fun handle(event: FileImportEvent) {
        when (event) {
            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            DoneButtonClicked -> {
                val result = fileImporter.import()
                if (result[0] is ImportResult.Success) {
                    navigator.navigateUp()
                }
            }

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
                    val screenState = DeckChooserScreenState(
                        purpose = DeckChooserScreenState.Purpose.ChooseDeckWhereToImportCards
                    )
                    DeckChooserDiScope.create(screenState)
                }
            }

            is TargetDeckIsSelected -> {
                val deck = ExistingDeck(event.deck)
                fileImporter.setDeckWhereToAdd(deck)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(fileImporter.state)
    }
}