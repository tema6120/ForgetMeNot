package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.ImportResult
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportController.Command.Navigate
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
                val result = fileImporter.import()
                if (result[0] is ImportResult.Success) {
                    navigator.navigateUp()
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(fileImporter.state)
    }
}