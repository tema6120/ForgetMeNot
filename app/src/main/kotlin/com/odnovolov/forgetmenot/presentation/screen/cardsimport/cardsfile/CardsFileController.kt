package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile

import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.entity.ExistingDeck
import com.odnovolov.forgetmenot.domain.entity.NewDeck
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToImportCards
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportScreenState
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.CardsFileController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.CardsFileController.Command.AskToUseSelectedDeckForImportNextFiles
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.CardsFileEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToRenameNewDeckForFileImport
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CardsFileController(
    private val cardsImporter: CardsImporter,
    private val navigator: Navigator,
    private val screenState: CardsImportScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsImporterStateProvider: ShortTermStateProvider<CardsImporter.State>,
    private val screenStateProvider: ShortTermStateProvider<CardsImportScreenState>
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
                val abstractDeck: AbstractDeck = with(cardsImporter.state) {
                    files[currentPosition].deckWhereToAdd
                }
                if (abstractDeck is NewDeck) {
                    navigator.showRenameDeckDialogFromCardsImport {
                        val dialogState = RenameDeckDialogState(
                            purpose = ToRenameNewDeckForFileImport,
                            typedDeckName = abstractDeck.deckName
                        )
                        RenameDeckDiScope.create(dialogState)
                    }
                }
            }

            AddCardsToNewDeckButtonClicked -> {
                navigator.showRenameDeckDialogFromCardsImport {
                    val state = RenameDeckDialogState(purpose = ToRenameNewDeckForFileImport)
                    RenameDeckDiScope.create(state)
                }
            }

            is SubmittedNameForNewDeck -> {
                val newDeck = NewDeck(event.deckName)
                cardsImporter.setDeckWhereToAdd(newDeck)
            }

            AddCardsToExistingDeckButtonClicked -> {
                navigator.navigateToDeckChooserFromCardsImport {
                    val screenState = DeckChooserScreenState(purpose = ToImportCards)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is TargetDeckWasSelected -> {
                val deck = ExistingDeck(event.deck)
                cardsImporter.setDeckWhereToAdd(deck)
                if (cardsImporter.state.maxVisitedPosition < cardsImporter.state.files.lastIndex
                    && cardsImporter.state.currentPosition == cardsImporter.state.maxVisitedPosition
                    && !screenState.wasAskedToUseSelectedDeckForImportNextFiles
                ) {
                    val nameOfSelectedDeck: String = event.deck.name
                    val countOfNextFiles: Int =
                        cardsImporter.state.files.lastIndex - cardsImporter.state.currentPosition
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
                cardsImporter.useCurrentDeckForNextFiles()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsImporterStateProvider.save(cardsImporter.state)
        screenStateProvider.save(screenState)
    }
}