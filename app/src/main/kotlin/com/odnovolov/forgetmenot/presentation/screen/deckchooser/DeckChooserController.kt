package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.DeckToCopyCardToIsSelected
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.DeckToMoveCardToIsSelected
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.DeckToCopyCardsToIsSelected
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.DeckToMoveCardsToIsSelected
import com.odnovolov.forgetmenot.presentation.screen.fileimport.FileImportDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.CardsFileEvent.TargetDeckIsSelected
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.DeckToMergeIntoIsSelected
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogPurpose.ToCreateNewForDeckChooser
import com.odnovolov.forgetmenot.presentation.screen.renamedeck.RenameDeckDialogState
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent

class DeckChooserController(
    private val deckReviewPreference: DeckReviewPreference,
    private val screenState: DeckChooserScreenState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<DeckChooserScreenState>
) : BaseController<DeckChooserEvent, Nothing>() {
    override fun handle(event: DeckChooserEvent) {
        when (event) {
            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            is SearchTextChanged -> {
                screenState.searchText = event.searchText
            }

            is DeckListSelected -> {
                val deckList: DeckList? = event.deckListId?.let { deckListId: Long ->
                    globalState.deckLists.find { deckList: DeckList -> deckList.id == deckListId }
                }
                deckReviewPreference.deckList = deckList
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
                val deck: Deck = globalState.decks.first { it.id == event.deckId }
                when (screenState.purpose) {
                    ToImportCards -> {
                        FileImportDiScope.getOrRecreate().cardsFileController
                            .dispatch(TargetDeckIsSelected(deck))
                    }
                    ToMergeInto -> {
                        val abstractDeck = ExistingDeck(deck)
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(DeckToMergeIntoIsSelected(abstractDeck))
                    }
                    ToMoveCard -> {
                        val abstractDeck = ExistingDeck(deck)
                        CardsEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToMoveCardToIsSelected(abstractDeck))
                    }
                    ToCopyCard -> {
                        val abstractDeck = ExistingDeck(deck)
                        CardsEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToCopyCardToIsSelected(abstractDeck))
                    }
                    ToMoveCardsInDeckEditor -> {
                        val abstractDeck = ExistingDeck(deck)
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToMoveCardsToIsSelected(abstractDeck))
                    }
                    ToCopyCardsInDeckEditor -> {
                        val abstractDeck = ExistingDeck(deck)
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToCopyCardsToIsSelected(abstractDeck))
                    }
                    ToMoveCardsInSearch -> {
                        val abstractDeck = ExistingDeck(deck)
                        SearchDiScope.getOrRecreate().controller
                            .dispatch(SearchEvent.DeckToMoveCardsToIsSelected(abstractDeck))
                    }
                    ToCopyCardsInSearch -> {
                        val abstractDeck = ExistingDeck(deck)
                        SearchDiScope.getOrRecreate().controller
                            .dispatch(SearchEvent.DeckToCopyCardsToIsSelected(abstractDeck))
                    }
                    ToMoveCardsInHomeSearch -> {
                        val abstractDeck = ExistingDeck(deck)
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(HomeEvent.DeckToMoveCardsToIsSelected(abstractDeck))
                    }
                    ToCopyCardsInHomeSearch -> {
                        val abstractDeck = ExistingDeck(deck)
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(HomeEvent.DeckToCopyCardsToIsSelected(abstractDeck))
                    }
                }
                navigator.navigateUp()
            }

            AddDeckButtonClicked -> {
                navigator.showRenameDeckDialogFromDeckChooser {
                    val dialogState = RenameDeckDialogState(purpose = ToCreateNewForDeckChooser)
                    RenameDeckDiScope.create(dialogState)
                }
            }

            is SubmittedNewDeckName -> {
                val abstractDeck = NewDeck(event.deckName)
                when (screenState.purpose) {
                    ToImportCards -> {}
                    ToMergeInto -> {
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(DeckToMergeIntoIsSelected(abstractDeck))
                    }
                    ToMoveCard -> {
                        CardsEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToMoveCardToIsSelected(abstractDeck))
                    }
                    ToCopyCard -> {
                        CardsEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToCopyCardToIsSelected(abstractDeck))
                    }
                    ToMoveCardsInDeckEditor -> {
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToMoveCardsToIsSelected(abstractDeck))
                    }
                    ToCopyCardsInDeckEditor -> {
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckToCopyCardsToIsSelected(abstractDeck))
                    }
                    ToMoveCardsInSearch -> {
                        SearchDiScope.getOrRecreate().controller
                            .dispatch(SearchEvent.DeckToMoveCardsToIsSelected(abstractDeck))
                    }
                    ToCopyCardsInSearch -> {
                        SearchDiScope.getOrRecreate().controller
                            .dispatch(SearchEvent.DeckToCopyCardsToIsSelected(abstractDeck))
                    }
                    ToMoveCardsInHomeSearch -> {
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(HomeEvent.DeckToMoveCardsToIsSelected(abstractDeck))
                    }
                    ToCopyCardsInHomeSearch -> {
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(HomeEvent.DeckToCopyCardsToIsSelected(abstractDeck))
                    }
                }
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}