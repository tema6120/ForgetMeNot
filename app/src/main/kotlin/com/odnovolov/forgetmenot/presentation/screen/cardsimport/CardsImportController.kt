package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NewDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.addDeckIds
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.ImportResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.ImportResult.Failure.Cause.InvalidName
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.ImportResult.Failure.Cause.NoCards
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.ImportResult.Success
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportController.Command.Navigate.FilePageTransition
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportController.Command.Navigate.FilePageTransition.*
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope

class CardsImportController(
    private val cardsImporter: CardsImporter,
    private val navigator: Navigator,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver,
    private val fileImporterStateProvider: ShortTermStateProvider<State>
) : BaseController<CardsImportEvent, Command>() {
    sealed class Command() {
        class Navigate(
            val cardsFileId: Long,
            val filePageTransition: FilePageTransition
        ) : Command() {
            enum class FilePageTransition {
                SwipeToNext,
                SwipeToPrevious,
                SwipeToNextDroppingCurrent,
                SwipeToPreviousDroppingCurrent
            }
        }

        class ShowMessageNumberOfImportedCards(val numberOfImportedCards: Int) : Command()
        object ShowMessageNoCardsToImport : Command()
        object ShowMessageInvalidDeckName : Command()
        object AskToImportIgnoringErrors : Command()
        object AskToConfirmExit : Command()
    }

    override fun handle(event: CardsImportEvent) {
        when (event) {
            is CardsFileWasOpened -> {
                val newPosition: Int =
                    cardsImporter.state.files.indexOfFirst { it.id == event.cardsFileId }
                if (newPosition == -1) return
                cardsImporter.setCurrentPosition(newPosition)
            }

            PreviousButtonClicked -> {
                with(cardsImporter.state) {
                    val canNavigateToPreviousPage: Boolean = currentPosition != 0
                    if (!canNavigateToPreviousPage) return
                    val cardsFileId: Long = files[currentPosition - 1].id
                    sendCommand(Navigate(cardsFileId, SwipeToPrevious))
                }
            }

            NextButtonClicked -> {
                with(cardsImporter.state) {
                    val canNavigateToNextPage: Boolean = currentPosition != files.lastIndex
                    if (!canNavigateToNextPage) return
                    val cardsFileId: Long = files[currentPosition + 1].id
                    sendCommand(Navigate(cardsFileId, SwipeToNext))
                }
            }

            SkipButtonClicked -> {
                with(cardsImporter.state) {
                    val cardsFileId: Long
                    val transition: FilePageTransition
                    when {
                        files.size <= 1 -> return
                        currentPosition == files.lastIndex -> {
                            cardsFileId = files[currentPosition - 1].id
                            transition = SwipeToPreviousDroppingCurrent
                        }
                        else -> {
                            cardsFileId = files[currentPosition + 1].id
                            transition = SwipeToNextDroppingCurrent
                        }
                    }
                    cardsImporter.skip()
                    sendCommand(Navigate(cardsFileId, transition))
                }
            }

            DoneButtonClicked -> {
                val hasErrors = cardsImporter.state.files.any { it.errors.isNotEmpty() }
                if (hasErrors) {
                    sendCommand(AskToImportIgnoringErrors)
                    return
                }
                tryToImport()
            }

            CancelButtonClicked, BackButtonClicked -> {
                sendCommand(AskToConfirmExit)
            }

            FixErrorsButtonClicked -> {
                for ((position: Int, file: CardsFile) in cardsImporter.state.files.withIndex()) {
                    if (file.errors.isEmpty()) continue
                    val isLastPosition = position == cardsImporter.state.files.lastIndex
                    if (!isLastPosition) {
                        sendCommand(Navigate(file.id, SwipeToPrevious))
                    }
                    return
                }
            }

            ImportIgnoringErrorsButtonClicked -> {
                tryToImport()
            }

            UserConfirmedExit -> {
                navigator.navigateUp()
            }
        }
    }

    private fun tryToImport() {
        when (val result = cardsImporter.import()) {
            is Success -> {
                sendCommand(ShowMessageNumberOfImportedCards(result.numberOfImportedCards))
                addNewDecksToCurrentViewingDeckList(result)
                if (result.decks.size == 1 &&
                    cardsImporter.state.files.first().deckWhereToAdd is NewDeck
                ) {
                    navigator.navigateToDeckEditorFromCardsImport {
                        val deck = result.decks[0]
                        val tabs = DeckEditorTabs.All(initialTab = DeckEditorScreenTab.Settings)
                        val screenState = DeckEditorScreenState(deck, tabs)
                        val batchCardEditor = BatchCardEditor(globalState)
                        DeckEditorDiScope.create(screenState, batchCardEditor)
                    }
                } else {
                    navigator.navigateUp()
                }
            }
            is Failure -> {
                when (val cause = result.cause) {
                    NoCards -> {
                        sendCommand(ShowMessageNoCardsToImport)
                    }
                    is InvalidName -> {
                        sendCommand(ShowMessageInvalidDeckName)
                        if (cause.position != cardsImporter.state.currentPosition) {
                            val filePageTransition: FilePageTransition =
                                if (cause.position > cardsImporter.state.currentPosition)
                                    SwipeToNext else
                                    SwipeToPrevious
                            val cardsFileId = cardsImporter.state.files[cause.position].id
                            sendCommand(Navigate(cardsFileId, filePageTransition))
                        }
                    }
                }
            }
        }
    }

    private fun addNewDecksToCurrentViewingDeckList(result: Success) {
        val deckListToView: DeckList? = HomeDiScope.getOrRecreate().deckReviewPreference.deckList
        deckListToView?.let { deckList: DeckList ->
            val newDeckIds: List<Long> = result.decks.mapIndexedNotNull { index, deck ->
                if (cardsImporter.state.files[index].deckWhereToAdd is NewDeck) {
                    deck.id
                } else {
                    null
                }
            }
            deckList.addDeckIds(newDeckIds)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(cardsImporter.state)
    }
}