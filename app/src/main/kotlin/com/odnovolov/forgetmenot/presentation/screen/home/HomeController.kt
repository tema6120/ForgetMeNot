package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.domain.interactor.deckexporter.DeckExporter
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.deckremover.DeckRemover.Event.DecksHasRemoved
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupScreenState
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsDiScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.IOException

class HomeController(
    private val homeScreenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val deckExporter: DeckExporter,
    private val deckRemover: DeckRemover,
    private val exerciseStateCreator: ExerciseStateCreator,
    private val cardsSearcher: CardsSearcher,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val homeScreenStateProvider: ShortTermStateProvider<HomeScreenState>
) : BaseController<HomeEvent, Command>() {
    sealed class Command {
        object ShowNoCardIsReadyForExerciseMessage : Command()
        class ShowDeckRemovingMessage(val numberOfDecksRemoved: Int) : Command()
        class ShowCreateFileDialog(val fileName: String) : Command()
        object ShowDeckIsExportedMessage : Command()
        class ShowExportErrorMessage(val e: IOException) : Command()
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
    }


    lateinit var displayedDeckIds: Flow<List<Long>>

    override fun handle(event: HomeEvent) {
        when (event) {
            is SearchTextChanged -> {
                homeScreenState.searchText = event.searchText
                cardsSearcher.search(event.searchText)
            }

            DisplayOnlyWithTasksCheckboxClicked -> {
                with(deckReviewPreference) { displayOnlyWithTasks = !displayOnlyWithTasks }
            }

            is FileForExportDeckIsReady -> {
                try {
                    deckExporter.export(
                        deck = homeScreenState.exportedDeck ?: return,
                        outputStream = event.outputStream
                    )
                    sendCommand(ShowDeckIsExportedMessage)
                } catch (e: IOException) {
                    sendCommand(ShowExportErrorMessage(e))
                }
            }

            DecksRemovedSnackbarCancelButtonClicked -> {
                deckRemover.restoreDecks()
            }

            SelectionCancelled -> {
                homeScreenState.deckSelection = null
            }

            SelectAllDecksButtonClicked -> {
                val allDisplayedDeckIds: List<Long> = displayedDeckIds.firstBlocking()
                homeScreenState.deckSelection =
                    homeScreenState.deckSelection?.copy(selectedDeckIds = allDisplayedDeckIds)
            }

            RemoveDecksButtonClicked -> {
                val deckIdsToRemove: List<Long> =
                    homeScreenState.deckSelection?.selectedDeckIds ?: return
                deckRemover.removeDecks(deckIdsToRemove)
                homeScreenState.deckSelection = null
            }

            is DeckButtonClicked -> {
                if (homeScreenState.deckSelection != null) {
                    toggleDeckSelection(event.deckId)
                } else {
                    navigateToDeckSetup(event.deckId)
                }
            }

            is DeckButtonLongClicked -> {
                toggleDeckSelection(event.deckId)
            }

            is DeckSelectorClicked -> {
                toggleDeckSelection(event.deckId)
            }

            is StartExerciseDeckOptionSelected -> {
                startExercise(deckIds = listOf(event.deckId))
            }

            is AutoplayDeckOptionSelected -> {
                navigateToAutoplaySettings(deckIds = listOf(event.deckId))
            }

            is ShowCardsDeckOptionSelected -> {
                navigateToDeckSetup(event.deckId)
            }

            is SetupDeckOptionSelected -> {
                navigateToDeckSetup(event.deckId)
            }

            is ExportDeckOptionSelected -> {
                val deck: Deck = globalState.decks.first { it.id == event.deckId }
                homeScreenState.exportedDeck = deck
                val fileName = deck.name
                sendCommand(ShowCreateFileDialog(fileName))
            }

            is RemoveDeckOptionSelected -> {
                deckRemover.removeDeck(event.deckId)
            }

            AutoplayButtonClicked -> {
                homeScreenState.deckSelection?.let { deckSelection: DeckSelection ->
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
                    homeScreenState.deckSelection = DeckSelection(
                        selectedDeckIds = emptyList(),
                        purpose = DeckSelection.Purpose.ForAutoplay
                    )
                }
            }

            ExerciseButtonClicked -> {
                homeScreenState.deckSelection?.let { deckSelection: DeckSelection ->
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
                    homeScreenState.deckSelection = DeckSelection(
                        selectedDeckIds = emptyList(),
                        purpose = DeckSelection.Purpose.ForExercise
                    )
                }
            }

            is FoundCardClicked -> {
                homeScreenState.deckSelection = null
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

    private fun navigateToDeckSetup(deckId: Long) {
        homeScreenState.deckSelection = null
        navigator.navigateToDeckSetupFromNavHost {
            val deck: Deck = globalState.decks.first { it.id == deckId }
            val deckEditorState = DeckEditor.State(deck)
            DeckSetupDiScope.create(DeckSetupScreenState(deck), deckEditorState)
        }
    }

    private fun toggleDeckSelection(deckId: Long) {
        homeScreenState.deckSelection?.let { deckSelection: DeckSelection ->
            val newSelectedDeckIds =
                if (deckId in deckSelection.selectedDeckIds)
                    deckSelection.selectedDeckIds - deckId else
                    deckSelection.selectedDeckIds + deckId
            if (newSelectedDeckIds.isEmpty()
                && deckSelection.purpose == DeckSelection.Purpose.General
            ) {
                homeScreenState.deckSelection = null
            } else {
                homeScreenState.deckSelection =
                    deckSelection.copy(selectedDeckIds = newSelectedDeckIds)
            }
        } ?: kotlin.run {
            homeScreenState.deckSelection = DeckSelection(
                selectedDeckIds = listOf(deckId),
                purpose = DeckSelection.Purpose.General
            )
        }
    }

    private fun navigateToAutoplaySettings(deckIds: List<Long>) {
        navigator.navigateToAutoplaySettings {
            val decks: List<Deck> = globalState.decks.filter { it.id in deckIds }
            val repetitionCreatorState = RepetitionStateCreator.State(decks)
            RepetitionSettingsDiScope.create(repetitionCreatorState, PresetDialogState())
        }
        homeScreenState.deckSelection = null
    }

    private fun startExercise(deckIds: List<Long>) {
        if (exerciseStateCreator.hasAnyCardAvailableForExercise(deckIds)) {
            navigator.navigateToExercise {
                val exerciseState: Exercise.State = exerciseStateCreator.create(deckIds)
                ExerciseDiScope.create(exerciseState)
            }
            homeScreenState.deckSelection = null
        } else {
            sendCommand(ShowNoCardIsReadyForExerciseMessage)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        homeScreenStateProvider.save(homeScreenState)
    }
}