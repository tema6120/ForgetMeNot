package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.NewDeck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState

class FileImportController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<FileImportEvent, Nothing>() {
    override fun handle(event: FileImportEvent) {
        when (event) {
            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            DoneButtonClicked -> {
                val result = fileImporter.import()
                if (result[0]) {
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
                            abstractDeck = abstractDeck,
                            typedDeckName = abstractDeck.deckName
                        )
                        RenameDeckDiScope.create(dialogState)
                    }
                }
            }

            AddCardsToNewDeckButtonClicked -> {
                val newDeck = NewDeck(deckName = "")
                fileImporter.setDeckWhereToAdd(newDeck)
                navigator.showRenameDeckDialogFromFileImport {
                    val dialogState = RenameDeckDialogState(
                        abstractDeck = newDeck,
                        typedDeckName = newDeck.deckName
                    )
                    RenameDeckDiScope.create(dialogState)
                }
            }

            AddCardsToExistingDeckButtonClicked -> {

            }

            is TextChanged -> {
                fileImporter.updateText(event.newText)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}