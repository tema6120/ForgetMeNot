package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayerStateCreator
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover.Event.DecksHasRemoved
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.CardFilterForAutoplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.GotFilesCreationResult.FileCreationResult
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToRenameExistingDeck
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.OutputStream

class HomeController(
    private val screenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val deckExporter: DeckExporter,
    private val deckRemover: DeckRemover,
    private val exerciseStateCreator: ExerciseStateCreator,
    private val cardsSearcher: CardsSearcher,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<HomeScreenState>
) : BaseController<HomeEvent, Command>() {
    sealed class Command {
        object ShowNoCardIsReadyForExerciseMessage : Command()
        object ShowDeckOption : Command()
        class ShowDeckRemovingMessage(val numberOfDecksRemoved: Int) : Command()
        class CreateFiles(val fileNames: List<String>) : Command()
        class ShowDeckExportResultMessage(
            val exportedDeckNames: List<String>,
            val failedDeckNames: List<String>
        ) : Command()
    }

    init {
        deckRemover.events.onEach { event: DeckRemover.Event ->
            when (event) {
                is DecksHasRemoved -> {
                    sendCommand(ShowDeckRemovingMessage(numberOfDecksRemoved = event.count))
                }
            }
        }
            .launchIn(coroutineScope)
        if (screenState.searchText.isNotEmpty()) {
            cardsSearcher.search(screenState.searchText)
        }
    }

    lateinit var displayedDeckIds: Flow<List<Long>>

    override fun handle(event: HomeEvent) {
        when (event) {
            is SearchTextChanged -> {
                screenState.searchText = event.searchText
                cardsSearcher.search(event.searchText)
            }

            is GotFilesCreationResult -> {
                val fileFormat: FileFormat = screenState.fileFormatForExport ?: return
                val exportedDeckNames: MutableList<String> = ArrayList()
                val failedDeckNames: MutableList<String> = ArrayList()
                for (fileCreationResult: FileCreationResult in event.filesCreationResult) {
                    val (fileName: String, outputStream: OutputStream?) = fileCreationResult
                    if (outputStream == null) {
                        failedDeckNames.add(fileName)
                        continue
                    }
                    val deckName = fileName.substringBeforeLast(".")
                    val deck = globalState.decks.first { deck: Deck -> deck.name == deckName }
                    val success: Boolean = deckExporter.export(deck, fileFormat, outputStream)
                    if (success) {
                        exportedDeckNames.add(fileName)
                    } else {
                        failedDeckNames.add(fileName)
                    }
                }
                sendCommand(ShowDeckExportResultMessage(exportedDeckNames, failedDeckNames))
            }

            DecksRemovedSnackbarCancelButtonClicked -> {
                deckRemover.restoreDecks()
            }

            is SelectedFileFormatForExport -> {
                screenState.fileFormatForExport = event.fileFormat
                val selectedDeckIds: List<Long> =
                    screenState.deckSelection?.selectedDeckIds ?: return
                val fileNames: List<String> = globalState.decks
                    .filter { deck: Deck -> deck.id in selectedDeckIds }
                    .map { deck: Deck -> "${deck.name}.${event.fileFormat.extension}" }
                sendCommand(CreateFiles(fileNames = fileNames))
            }

            SelectionCancelled -> {
                screenState.deckSelection = null
            }

            SelectAllDecksButtonClicked -> {
                val allDisplayedDeckIds: List<Long> = displayedDeckIds.firstBlocking()
                screenState.deckSelection =
                    screenState.deckSelection?.copy(selectedDeckIds = allDisplayedDeckIds)
            }

            RemoveDecksButtonClicked -> {
                val deckIdsToRemove: List<Long> =
                    screenState.deckSelection?.selectedDeckIds ?: return
                deckRemover.removeDecks(deckIdsToRemove)
                screenState.deckSelection = null
            }

            DecksAvailableForExerciseCheckboxClicked -> {
                with(deckReviewPreference) {
                    displayOnlyDecksAvailableForExercise = !displayOnlyDecksAvailableForExercise
                }
            }

            SortingDirectionButtonClicked -> {
                with(deckReviewPreference) {
                    val newDirection = if (deckSorting.direction == Asc) Desc else Asc
                    deckSorting = deckSorting.copy(direction = newDirection)
                }
            }

            is SortByButtonClicked -> {
                with(deckReviewPreference) {
                    deckSorting = if (event.criterion == deckSorting.criterion) {
                        val newDirection = if (deckSorting.direction == Asc) Desc else Asc
                        deckSorting.copy(direction = newDirection)
                    } else {
                        deckSorting.copy(criterion = event.criterion)
                    }
                }
            }

            is DeckButtonClicked -> {
                if (screenState.deckSelection != null) {
                    toggleDeckSelection(event.deckId)
                } else {
                    startExercise(listOf(event.deckId))
                }
            }

            is DeckButtonLongClicked -> {
                toggleDeckSelection(event.deckId)
            }

            is DeckSelectorClicked -> {
                toggleDeckSelection(event.deckId)
            }

            is DeckOptionButtonClicked -> {
                val deck: Deck = globalState.decks.first { it.id == event.deckId }
                screenState.deckForDeckOptionMenu = deck
                sendCommand(ShowDeckOption)
            }

            StartExerciseDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                startExercise(deckIds = listOf(deckId))
            }

            AutoplayDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                navigateToAutoplaySettings(deckIds = listOf(deckId))
            }

            RenameDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                navigator.showRenameDeckDialogFromNavHost {
                    val deck = globalState.decks.first { it.id == deckId }
                    val dialogState = RenameDeckDialogState(
                        purpose = ToRenameExistingDeck(deck),
                        typedDeckName = deck.name
                    )
                    RenameDeckDiScope.create(dialogState)
                }
            }

            EditCardsDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                navigateToDeckEditor(deckId, DeckEditorScreenTab.Content)
            }

            SetupDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                navigateToDeckEditor(deckId, DeckEditorScreenTab.Settings)
            }

            ExportDeckOptionSelected -> {
                val fileName = screenState.deckForDeckOptionMenu?.name ?: return
                val fileNames = listOf(fileName)
                sendCommand(CreateFiles(fileNames))
            }

            RemoveDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                deckRemover.removeDeck(deckId)
            }

            AutoplayButtonClicked -> {
                screenState.deckSelection?.let { deckSelection: DeckSelection ->
                    if (deckSelection.selectedDeckIds.isEmpty()
                        || deckSelection.purpose !in listOf(
                            DeckSelection.Purpose.General,
                            DeckSelection.Purpose.ForAutoplay
                        )
                    ) {
                        return
                    }
                    navigateToAutoplaySettings(deckSelection.selectedDeckIds)
                } ?: kotlin.run {
                    screenState.deckSelection = DeckSelection(
                        selectedDeckIds = emptyList(),
                        purpose = DeckSelection.Purpose.ForAutoplay
                    )
                }
            }

            ExerciseButtonClicked -> {
                screenState.deckSelection?.let { deckSelection: DeckSelection ->
                    if (deckSelection.selectedDeckIds.isEmpty()
                        || deckSelection.purpose !in listOf(
                            DeckSelection.Purpose.General,
                            DeckSelection.Purpose.ForExercise
                        )
                    ) {
                        return
                    }
                    startExercise(deckSelection.selectedDeckIds)
                } ?: kotlin.run {
                    screenState.deckSelection = DeckSelection(
                        selectedDeckIds = emptyList(),
                        purpose = DeckSelection.Purpose.ForExercise
                    )
                }
            }

            is FoundCardClicked -> {
                screenState.deckSelection = null
                navigator.navigateToCardsEditorFromNavHost {
                    val editableCard = EditableCard(
                        event.searchCard.card,
                        event.searchCard.deck
                    )
                    val editableCards: List<EditableCard> = listOf(editableCard)
                    val cardsEditorState = State(editableCards)
                    val cardsEditor = CardsEditorForEditingSpecificCards(
                        state = cardsEditorState
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }
        }
    }

    private fun navigateToDeckEditor(
        deckId: Long,
        initialTab: DeckEditorScreenTab
    ) {
        screenState.deckSelection = null
        navigator.navigateToDeckEditorFromNavHost {
            val deck: Deck = globalState.decks.first { it.id == deckId }
            val tabs = DeckEditorTabs.All(initialTab)
            val screenState = DeckEditorScreenState(deck, tabs)
            DeckEditorDiScope.create(screenState)
        }
    }

    private fun toggleDeckSelection(deckId: Long) {
        screenState.deckSelection?.let { deckSelection: DeckSelection ->
            val newSelectedDeckIds =
                if (deckId in deckSelection.selectedDeckIds)
                    deckSelection.selectedDeckIds - deckId else
                    deckSelection.selectedDeckIds + deckId
            if (newSelectedDeckIds.isEmpty()
                && deckSelection.purpose == DeckSelection.Purpose.General
            ) {
                screenState.deckSelection = null
            } else {
                screenState.deckSelection =
                    deckSelection.copy(selectedDeckIds = newSelectedDeckIds)
            }
        } ?: kotlin.run {
            screenState.deckSelection = DeckSelection(
                selectedDeckIds = listOf(deckId),
                purpose = DeckSelection.Purpose.General
            )
        }
    }

    private fun navigateToAutoplaySettings(deckIds: List<Long>) {
        screenState.deckSelection = null
        navigator.navigateToAutoplaySettings {
            val decks: List<Deck> = globalState.decks.filter { it.id in deckIds }
            val playerCreatorState = PlayerStateCreator.State(
                decks,
                globalState.cardFilterForAutoplay
            )
            CardFilterForAutoplayDiScope.create(playerCreatorState)
        }
    }

    private fun startExercise(deckIds: List<Long>) {
        if (exerciseStateCreator.hasAnyCardAvailableForExercise(deckIds)) {
            navigator.navigateToExercise {
                val exerciseState: Exercise.State = exerciseStateCreator.create(deckIds)
                ExerciseDiScope.create(exerciseState)
            }
            screenState.deckSelection = null
        } else {
            sendCommand(ShowNoCardIsReadyForExerciseMessage)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}