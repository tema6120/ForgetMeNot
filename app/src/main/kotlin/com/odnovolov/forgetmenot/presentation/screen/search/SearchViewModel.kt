package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import kotlinx.coroutines.flow.*

class SearchViewModel(
    val initialSearchText: String,
    searcherState: CardsSearcher.State,
    private val batchCardEditorState: BatchCardEditor.State
) {
    val searchDeckName: Flow<String?> = flow {
        val deckName: String? = DeckEditorDiScope.getAsync()?.let { diScope: DeckEditorDiScope ->
            diScope.screenState.deck.name
        }
        emit(deckName)
    }

    val foundCards: Flow<List<SelectableSearchCard>> = combine(
        searcherState.flowOf(CardsSearcher.State::searchResult),
        batchCardEditorState.flowOf(BatchCardEditor.State::selectedCards)
    ) { foundCards: List<SearchCard>, selectedCards: Collection<EditableCard> ->
        val selectedCardIds: List<Long> =
            selectedCards.map { editableCard: EditableCard -> editableCard.card.id }
        foundCards.map { searchCard: SearchCard ->
            val isSelected: Boolean = searchCard.card.id in selectedCardIds
            SelectableSearchCard(
                searchCard.card,
                searchCard.deck,
                searchCard.questionMatchingRanges,
                searchCard.answerMatchingRanges,
                isSelected
            )
        }
    }

    val isSearching: Flow<Boolean> = searcherState.flowOf(CardsSearcher.State::isSearching)

    val cardsNotFound: Flow<Boolean> = combine(
        searcherState.flowOf(CardsSearcher.State::searchText),
        isSearching,
        searcherState.flowOf(CardsSearcher.State::searchResult)
    ) { searchText: String, isSearching: Boolean, foundCards: List<SearchCard> ->
        searchText.isNotEmpty() && !isSearching && foundCards.isEmpty()
    }
        .distinctUntilChanged()

    val isSelectionMode: Flow<Boolean> =
        batchCardEditorState.flowOf(BatchCardEditor.State::selectedCards)
            .map { editableCards: Collection<EditableCard> -> editableCards.isNotEmpty() }

    val numberOfSelectedCards: Flow<Int> =
        batchCardEditorState.flowOf(BatchCardEditor.State::selectedCards)
            .map { editableCards: Collection<EditableCard> -> editableCards.size }

    val isMarkAsLearnedOptionAvailable: Boolean
        get() = batchCardEditorState.selectedCards.any { editableCard: EditableCard ->
            !editableCard.card.isLearned
        }

    val isMarkAsUnlearnedOptionAvailable: Boolean
        get() = batchCardEditorState.selectedCards.any { editableCard: EditableCard ->
            editableCard.card.isLearned
        }
}