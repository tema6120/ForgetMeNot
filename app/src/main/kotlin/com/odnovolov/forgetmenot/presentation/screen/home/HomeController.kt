package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayerStateCreator
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseStateCreator
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckMerger
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.DeckRemover
import com.odnovolov.forgetmenot.domain.interactor.operationsondecks.into
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.FoundCard
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforautoplay.CardFilterForAutoplayDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeCaller
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeDiScope
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeDialogState
import com.odnovolov.forgetmenot.presentation.screen.changegrade.GradeItem
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.DeckListsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.export.ExportDiScope
import com.odnovolov.forgetmenot.presentation.screen.export.ExportDialogState
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToRenameExistingDeck
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import kotlinx.coroutines.flow.Flow

class HomeController(
    private val screenState: HomeScreenState,
    private val deckReviewPreference: DeckReviewPreference,
    private val deckRemover: DeckRemover,
    private val deckMerger: DeckMerger,
    private val exerciseStateCreator: ExerciseStateCreator,
    private val cardsSearcher: CardsSearcher,
    private val batchCardEditor: BatchCardEditor,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<HomeScreenState>,
    private val batchCardEditorProvider: ShortTermStateProvider<BatchCardEditor>
) : BaseController<HomeEvent, Command>() {
    sealed class Command {
        object ShowNoCardIsReadyForExerciseMessage : Command()
        object ShowDeckOptions : Command()
        object ShowDeckSelectionOptions : Command()
        object ShowCardSelectionOptions : Command()
        class ShowDeckRemovingMessage(val numberOfRemovedDecks: Int) : Command()
        class ShowDeckMergingMessage(
            val numberOfMergedDecks: Int,
            val deckNameMergedInto: String
        ) : Command()

        class ShowCardsAreInvertedMessage(val numberOfInvertedCards: Int) : Command()
        class ShowGradeIsChangedMessage(val grade: Int, val numberOfAffectedCards: Int) : Command()
        class ShowCardsAreMarkedAsLearnedMessage(val numberOfMarkedCards: Int) : Command()
        class ShowCardsAreMarkedAsUnlearnedMessage(val numberOfMarkedCards: Int) : Command()
        class ShowCardsAreRemovedMessage(val numberOfRemovedCards: Int) : Command()
        class ShowCardsAreMovedMessage(
            val numberOfMovedCards: Int,
            val deckNameToWhichCardsWereMoved: String
        ) : Command()

        class ShowCardsAreCopiedMessage(
            val numberOfCopiedCards: Int,
            val deckNameToWhichCardsWereCopied: String
        ) : Command()
    }

    init {
        if (screenState.searchText.isNotEmpty()) {
            cardsSearcher.search(screenState.searchText)
        }
    }

    lateinit var displayedDeckIds: Flow<List<Long>>
    private var needToResearchOnCancel = false

    override fun handle(event: HomeEvent) {
        when (event) {
            is SearchTextChanged -> {
                screenState.searchText = event.searchText
                cardsSearcher.search(event.searchText)
            }

            DecksAvailableForExerciseCheckboxClicked -> {
                with(deckReviewPreference) {
                    displayOnlyDecksAvailableForExercise = !displayOnlyDecksAvailableForExercise
                }
            }

            EditDeckListsButtonClicked -> {
                navigator.navigateToDeckListsEditor {
                    val deckListsEditorState = DeckListsEditor.State.create(globalState)
                    DeckListsEditorDiScope.create(deckListsEditorState)
                }
            }

            is DeckListSelected -> {
                val selectedDeckList: DeckList? = event.deckListId?.let { deckListId: Long ->
                    globalState.deckLists.find { deckList: DeckList -> deckList.id == deckListId }
                }
                deckReviewPreference.currentDeckList = selectedDeckList
            }

            CreateDeckListButtonClicked -> {
                navigator.navigateToDeckListsEditor {
                    val deckListsEditorState = DeckListsEditor.State.create(globalState)
                    DeckListsEditorDiScope.create(deckListsEditorState)
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
                sendCommand(ShowDeckOptions)
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

            SetupDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                navigateToDeckEditor(deckId, DeckEditorScreenTab.Settings)
            }

            EditCardsDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                navigateToDeckEditor(deckId, DeckEditorScreenTab.Content)
            }

            PinDeckOptionSelected -> {
                screenState.deckForDeckOptionMenu?.isPinned = true
            }

            UnpinDeckOptionSelected -> {
                screenState.deckForDeckOptionMenu?.isPinned = false
            }

            ExportDeckOptionSelected -> {
                val deck = screenState.deckForDeckOptionMenu ?: return
                navigator.navigateToExportFromNavHost {
                    val dialogState = ExportDialogState(listOf(deck))
                    ExportDiScope.create(dialogState)
                }
            }

            MergeIntoDeckOptionSelected -> {
                navigateToDeckChooser()
            }

            RemoveDeckOptionSelected -> {
                val deckId: Long = screenState.deckForDeckOptionMenu?.id ?: return
                val numberOfRemovedDecks = deckRemover.removeDeck(deckId)
                sendCommand(ShowDeckRemovingMessage(numberOfRemovedDecks))
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
                val foundCard: FoundCard = cardsSearcher.state.searchResult
                    .find { it.card.id == event.cardId } ?: return
                if (isCardSelection()) {
                    toggleCardSelection(foundCard)
                } else {
                    navigateToCardsEditor(foundCard)
                }
            }

            is FoundCardLongClicked -> {
                val foundCard: FoundCard = cardsSearcher.state.searchResult
                    .find { it.card.id == event.cardId } ?: return
                toggleCardSelection(foundCard)
            }

            CancelledSelection -> {
                when {
                    isDeckSelection() -> screenState.deckSelection = null
                    isCardSelection() -> batchCardEditor.clearSelection()
                }
            }

            SelectAllSelectionToolbarButtonClicked -> {
                when {
                    isDeckSelection() -> {
                        val allDisplayedDeckIds: List<Long> = displayedDeckIds.firstBlocking()
                        screenState.deckSelection =
                            screenState.deckSelection?.copy(selectedDeckIds = allDisplayedDeckIds)
                    }
                    isCardSelection() -> {
                        val allEditableCards: List<EditableCard> =
                            cardsSearcher.state.searchResult.map { foundCard: FoundCard ->
                                EditableCard(foundCard.card, foundCard.deck)
                            }
                        batchCardEditor.addCardsToSelection(allEditableCards)
                    }
                }
            }

            RemoveSelectionToolbarButtonClicked -> {
                when {
                    isDeckSelection() -> removeSelectedDecks()
                    isCardSelection() -> removeSelectedCards()
                }
            }

            MoreSelectionToolbarButtonClicked -> {
                when {
                    isDeckSelection() -> sendCommand(ShowDeckSelectionOptions)
                    isCardSelection() -> sendCommand(ShowCardSelectionOptions)
                }
            }

            PinDeckSelectionOptionSelected -> {
                val selectedDeckIds: List<Long> =
                    screenState.deckSelection?.selectedDeckIds ?: return
                for (deck in globalState.decks) {
                    if (deck.id in selectedDeckIds && !deck.isPinned) {
                        deck.isPinned = true
                    }
                }
                screenState.deckSelection = null
            }

            UnpinDeckSelectionOptionSelected -> {
                val selectedDeckIds: List<Long> =
                    screenState.deckSelection?.selectedDeckIds ?: return
                for (deck in globalState.decks) {
                    if (deck.id in selectedDeckIds && deck.isPinned) {
                        deck.isPinned = false
                    }
                }
                screenState.deckSelection = null
            }

            ExportDeckSelectionOptionSelected -> {
                val selectedDeckIds: List<Long> =
                    screenState.deckSelection?.selectedDeckIds ?: return
                if (selectedDeckIds.isEmpty()) return
                navigator.navigateToExportFromNavHost {
                    val decks: List<Deck> =
                        globalState.decks.filter { deck: Deck -> deck.id in selectedDeckIds }
                    val dialogState = ExportDialogState(decks)
                    ExportDiScope.create(dialogState)
                }
            }

            MergeIntoDeckSelectionOptionSelected -> {
                navigateToDeckChooser()
            }

            is DeckToMergeIntoIsSelected -> {
                val selectedDeckIds: List<Long> =
                    screenState.deckSelection?.selectedDeckIds
                        ?: screenState.deckForDeckOptionMenu?.let { deck -> listOf(deck.id) }
                        ?: return
                val selectedDecks: List<Deck> =
                    globalState.decks.filter { deck: Deck -> deck.id in selectedDeckIds }
                val numberOfMergedDecks = deckMerger.merge(selectedDecks into event.abstractDeck)
                if (numberOfMergedDecks > 0) {
                    val deckNameMergedInto: String = when (event.abstractDeck) {
                        is NewDeck -> event.abstractDeck.deckName
                        is ExistingDeck -> event.abstractDeck.deck.name
                        else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
                    }
                    sendCommand(
                        command = ShowDeckMergingMessage(numberOfMergedDecks, deckNameMergedInto),
                        postponeIfNotActive = true
                    )
                }
                screenState.deckSelection = null
            }

            MergedDecksSnackbarCancelButtonClicked -> {
                deckMerger.cancel()
            }

            RemoveDeckSelectionOptionSelected -> {
                removeSelectedDecks()
            }

            RemovedDecksSnackbarCancelButtonClicked -> {
                deckRemover.restoreDecks()
            }

            InvertCardSelectionOptionSelected -> {
                val numberOfInvertedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.invert()
                sendCommand(ShowCardsAreInvertedMessage(numberOfInvertedCards))
                needToResearchOnCancel = false
            }

            ChangeGradeCardSelectionOptionSelected -> {
                navigator.showChangeGradeDialogFromNavHost {
                    val dialogState = ChangeGradeDialogState(
                        gradeItems = determineGradeItems(),
                        caller = ChangeGradeCaller.HomeSearch
                    )
                    ChangeGradeDiScope.create(dialogState)
                }
            }

            is SelectedGrade -> {
                val numberOfAffectedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.changeGrade(event.grade)
                sendCommand(ShowGradeIsChangedMessage(event.grade, numberOfAffectedCards))
                needToResearchOnCancel = false
            }

            MarkAsLearnedCardSelectionOptionSelected -> {
                val numberOfMarkedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.markAsLearned()
                sendCommand(ShowCardsAreMarkedAsLearnedMessage(numberOfMarkedCards))
                needToResearchOnCancel = false
            }

            MarkAsUnlearnedCardSelectionOptionSelected -> {
                val numberOfMarkedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.markAsUnlearned()
                sendCommand(ShowCardsAreMarkedAsUnlearnedMessage(numberOfMarkedCards))
                needToResearchOnCancel = false
            }

            RemoveCardsCardSelectionOptionSelected -> {
                val numberOfRemovedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.remove()
                cardsSearcher.research()
                sendCommand(ShowCardsAreRemovedMessage(numberOfRemovedCards))
                needToResearchOnCancel = true
            }

            MoveCardSelectionOptionSelected -> {
                navigator.navigateToDeckChooserFromNavHost {
                    val screenState = DeckChooserScreenState(purpose = ToMoveCardsInHomeSearch)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToMoveCardsToIsSelected -> {
                val numberOfMovedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.moveTo(event.abstractDeck)
                val deckName: String = event.abstractDeck.name
                cardsSearcher.research()
                sendCommand(
                    command = ShowCardsAreMovedMessage(numberOfMovedCards, deckName),
                    postponeIfNotActive = true
                )
                needToResearchOnCancel = true
            }

            CopyCardSelectionOptionSelected -> {
                navigator.navigateToDeckChooserFromNavHost {
                    val screenState = DeckChooserScreenState(purpose = ToCopyCardsInHomeSearch)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToCopyCardsToIsSelected -> {
                val numberOfCopiedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.copyTo(event.abstractDeck)
                val deckName: String = event.abstractDeck.name
                cardsSearcher.research()
                sendCommand(
                    command = ShowCardsAreCopiedMessage(numberOfCopiedCards, deckName),
                    postponeIfNotActive = true
                )
                needToResearchOnCancel = true
            }

            CancelCardSelectionActionSnackbarButtonClicked -> {
                batchCardEditor.cancelLastAction()
                if (needToResearchOnCancel) {
                    needToResearchOnCancel = false
                    cardsSearcher.research()
                }
            }
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

    private fun navigateToDeckChooser() {
        navigator.navigateToDeckChooserFromNavHost {
            val screenState = DeckChooserScreenState(purpose = ToMergeInto)
            DeckChooserDiScope.create(screenState)
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
            val batchCardEditor = BatchCardEditor(globalState)
            DeckEditorDiScope.create(screenState, batchCardEditor)
        }
    }

    private fun navigateToCardsEditor(foundCard: FoundCard) {
        navigator.navigateToCardsEditorFromNavHost {
            val editableCard = EditableCard(foundCard.card, foundCard.deck)
            val editableCards: List<EditableCard> = listOf(editableCard)
            val cardsEditorState = State(editableCards)
            val cardsEditor = CardsEditorForEditingSpecificCards(
                cardsEditorState,
                globalState
            )
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    private fun isDeckSelection(): Boolean = screenState.deckSelection != null
    private fun isCardSelection(): Boolean = batchCardEditor.state.selectedCards.isNotEmpty()

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

    private fun toggleCardSelection(foundCard: FoundCard) {
        val editableCard = EditableCard(foundCard.card, foundCard.deck)
        batchCardEditor.toggleSelected(editableCard)
    }

    private fun removeSelectedDecks() {
        val deckIdsToRemove: List<Long> =
            screenState.deckSelection?.selectedDeckIds ?: return
        val numberOfRemovedDecks = deckRemover.removeDecks(deckIdsToRemove)
        sendCommand(ShowDeckRemovingMessage(numberOfRemovedDecks))
        screenState.deckSelection = null
    }

    private fun removeSelectedCards() {
        val numberOfRemovedCards: Int = batchCardEditor.state.selectedCards.size
        batchCardEditor.remove()
        cardsSearcher.research()
        sendCommand(ShowCardsAreRemovedMessage(numberOfRemovedCards))
        needToResearchOnCancel = true
    }

    private fun determineGradeItems(): List<GradeItem> {
        var baseIntervalScheme: IntervalScheme? = null
        for (foundCard: FoundCard in cardsSearcher.state.searchResult) {
            val intervalScheme: IntervalScheme =
                foundCard.deck.exercisePreference.intervalScheme ?: continue
            when {
                baseIntervalScheme == null -> {
                    baseIntervalScheme = intervalScheme
                }
                baseIntervalScheme.id != intervalScheme.id -> {
                    baseIntervalScheme = null
                    break
                }
            }
        }
        return baseIntervalScheme?.intervals?.map { interval: Interval ->
            GradeItem(
                grade = interval.grade,
                waitingPeriod = interval.value
            )
        } ?: listOf(
            GradeItem(0, null),
            GradeItem(1, null),
            GradeItem(2, null),
            GradeItem(3, null),
            GradeItem(4, null),
            GradeItem(5, null),
            GradeItem(6, null)
        )
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
        batchCardEditorProvider.save(batchCardEditor)
    }
}