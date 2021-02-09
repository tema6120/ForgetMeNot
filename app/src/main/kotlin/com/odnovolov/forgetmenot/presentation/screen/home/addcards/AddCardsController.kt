package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.InvalidNameException
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckCreator
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.domain.interactor.fileimport.ImportedFile
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsController.Command.SetDialogText
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsController.Command.ShowCannotReadFilesMessage
import com.odnovolov.forgetmenot.presentation.screen.home.addcards.AddCardsEvent.*

class AddCardsController(
    private val screenState: AddCardsScreenState,
    private val deckCreator: DeckCreator,
    private val fileFromIntentReader: FileFromIntentReader,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val addCardsScreenStateProvider: ShortTermStateProvider<AddCardsScreenState>
) : BaseController<AddCardsEvent, Command>() {
    sealed class Command {
        class ShowCannotReadFilesMessage(val fileNames: List<String?>) : Command()
        class SetDialogText(val text: String) : Command()
    }

    override fun handle(event: AddCardsEvent) {
        when (event) {
            HelpImportFileButtonClicked -> {
                navigator.navigateToHelpArticleFromNavHost {
                    val screenState = HelpArticleScreenState(HelpArticle.ImportOfDeck)
                    HelpArticleDiScope.create(screenState)
                }
            }

            AddCardsHereButtonClicked -> {
                screenState.isDeckBeingCreated = true
                sendCommand(SetDialogText(""))
            }

            is ReceivedContent -> {
                screenState.areFilesBeingReading = true
                val results: List<FileFromIntentReader.Result> =
                    fileFromIntentReader.read(event.intent)
                screenState.areFilesBeingReading = false
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

            is DialogTextChanged -> {
                screenState.typedText = event.dialogText
            }

            DialogOkButtonClicked -> {
                val cardsEditor: CardsEditor = try {
                    deckCreator.create(screenState.typedText)
                } catch (e: InvalidNameException) {
                    // asynchronous behavior issue
                    return
                }
                navigator.navigateToCardsEditorFromNavHost {
                    CardsEditorDiScope.create(cardsEditor)
                }
                screenState.isDeckBeingCreated = false
            }

            DialogCancelButtonClicked -> {
                screenState.isDeckBeingCreated = false
            }
        }
    }

    private fun navigateToDeckEditor(deck: Deck) {
        navigator.navigateToDeckEditorFromNavHost {
            val tabs = DeckEditorTabs.All(initialTab = DeckEditorScreenTab.Settings)
            val screenState = DeckEditorScreenState(deck, tabs)
            DeckEditorDiScope.create(screenState)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        addCardsScreenStateProvider.save(screenState)
    }
}