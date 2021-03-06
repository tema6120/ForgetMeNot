package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NewDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardsFile
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Failure.Cause.InvalidName
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Failure.Cause.NoCards
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult.Success
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate.FilePageTransition
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate.FilePageTransition.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*

class FileImportController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver,
    private val fileImporterStateProvider: ShortTermStateProvider<State>
) : BaseController<FileImportEvent, Command>() {
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

    override fun handle(event: FileImportEvent) {
        when (event) {
            is CardsFileIsOpened -> {
                val newPosition: Int =
                    fileImporter.state.files.indexOfFirst { it.id == event.cardsFileId }
                if (newPosition == -1) return
                fileImporter.setCurrentPosition(newPosition)
            }

            PreviousButtonClicked -> {
                with(fileImporter.state) {
                    val canNavigateToPreviousPage: Boolean = currentPosition != 0
                    if (!canNavigateToPreviousPage) return
                    val cardsFileId: Long = files[currentPosition - 1].id
                    sendCommand(Navigate(cardsFileId, SwipeToPrevious))
                }
            }

            NextButtonClicked -> {
                with(fileImporter.state) {
                    val canNavigateToNextPage: Boolean = currentPosition != files.lastIndex
                    if (!canNavigateToNextPage) return
                    val cardsFileId: Long = files[currentPosition + 1].id
                    sendCommand(Navigate(cardsFileId, SwipeToNext))
                }
            }

            SkipButtonClicked -> {
                with(fileImporter.state) {
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
                    fileImporter.skip()
                    sendCommand(Navigate(cardsFileId, transition))
                }
            }

            DoneButtonClicked -> {
                val hasErrors = fileImporter.state.files.any { it.errors.isNotEmpty() }
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
                for ((position: Int, file: CardsFile) in fileImporter.state.files.withIndex()) {
                    if (file.errors.isEmpty()) continue
                    val isLastPosition = position == fileImporter.state.files.lastIndex
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
        when (val result = fileImporter.import()) {
            is Success -> {
                sendCommand(ShowMessageNumberOfImportedCards(result.numberOfImportedCards))
                if (result.decks.size == 1 &&
                    fileImporter.state.files.first().deckWhereToAdd is NewDeck
                ) {
                    navigator.navigateToDeckEditorFromFileImport {
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
                        if (cause.position != fileImporter.state.currentPosition) {
                            val filePageTransition: FilePageTransition =
                                if (cause.position > fileImporter.state.currentPosition)
                                    SwipeToNext else
                                    SwipeToPrevious
                            val cardsFileId = fileImporter.state.files[cause.position].id
                            sendCommand(Navigate(cardsFileId, filePageTransition))
                        }
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(fileImporter.state)
    }
}