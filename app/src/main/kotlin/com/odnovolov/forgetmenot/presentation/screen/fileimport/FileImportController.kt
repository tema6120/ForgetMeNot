package com.odnovolov.forgetmenot.presentation.screen.fileimport

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
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate.FilePageTransition
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate.FilePageTransition.*
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportEvent.*

class FileImportController(
    private val fileImporter: FileImporter,
    private val navigator: Navigator,
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
        }
    }

    private fun tryToImport() {
        val result = fileImporter.import()
        when (result) {
            is Success -> {
                sendCommand(ShowMessageNumberOfImportedCards(result.numberOfImportedCards))
                // todo: navigate to deck settings
                navigator.navigateUp()
            }
            is Failure -> {
                when (val cause = result.cause) {
                    NoCards -> {
                        sendCommand(ShowMessageNoCardsToImport)
                    }
                    is InvalidName -> {
                        sendCommand(ShowMessageInvalidDeckName)
                        if (cause.position != fileImporter.state.files.lastIndex) {
                            val cardsFileId = fileImporter.state.files[cause.position].id
                            sendCommand(Navigate(cardsFileId, SwipeToPrevious))
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