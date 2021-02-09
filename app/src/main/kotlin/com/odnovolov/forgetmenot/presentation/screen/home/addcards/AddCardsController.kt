package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.ImportedFile
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsController.Command.ShowCannotReadFilesMessage
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToCreateNewDeck
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState

class AddCardsController(
    private val fileFromIntentReader: FileFromIntentReader,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<AddCardsEvent, Command>() {
    sealed class Command {
        class ShowCannotReadFilesMessage(val fileNames: List<String?>) : Command()
    }

    override fun handle(event: AddCardsEvent) {
        when (event) {
            is ReceivedContent -> {
                val results: List<FileFromIntentReader.Result> =
                    fileFromIntentReader.read(event.intent)
                val failedFileNames: List<String?> = results
                    .mapNotNull { result -> result as? FileFromIntentReader.Result.Failure }
                    .map { failure -> failure.fileName }
                if (failedFileNames.isNotEmpty()) {
                    sendCommand(ShowCannotReadFilesMessage(failedFileNames))
                }
                val importedFiles: List<ImportedFile> = results.mapNotNull { result ->
                    when (result) {
                        is FileFromIntentReader.Result.Success -> {
                            ImportedFile(
                                fileName = result.fileName ?: "",
                                content = result.fileContent
                            )
                        }
                        is FileFromIntentReader.Result.Failure -> null
                    }
                }
                if (importedFiles.isNotEmpty()) {
                    navigator.navigateToFileImport {
                        val fileImporterState = FileImporter.State.fromFiles(importedFiles)
                        FileImportDiScope.create(fileImporterState)
                    }
                }
            }


            HelpImportFileButtonClicked -> {
                navigator.navigateToHelpArticleFromNavHost {
                    val screenState = HelpArticleScreenState(HelpArticle.ImportOfDeck)
                    HelpArticleDiScope.create(screenState)
                }
            }

            CreateNewDeckButtonClicked -> {
                navigator.showRenameDeckDialogFromNavHost {
                    val state = RenameDeckDialogState(purpose = ToCreateNewDeck)
                    RenameDeckDiScope.create(state)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}