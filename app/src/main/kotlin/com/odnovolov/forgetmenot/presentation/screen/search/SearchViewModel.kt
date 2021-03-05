package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.FoundCard
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
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
    ) { foundCards: List<FoundCard>, selectedCards: Collection<EditableCard> ->
        val selectedCardIds: List<Long> =
            selectedCards.map { editableCard: EditableCard -> editableCard.card.id }
        foundCards.map { foundCard: FoundCard ->
                val isSelected: Boolean = foundCard.card.id in selectedCardIds
                SelectableSearchCard(
                    foundCard.card.id,
                    foundCard.card.question,
                    foundCard.card.answer,
                    foundCard.card.isLearned,
                    foundCard.card.grade,
                    foundCard.searchText,
                    isSelected
                )
            }
    }.flowOn(businessLogicThread)

    val isSearching: Flow<Boolean> = searcherState.flowOf(CardsSearcher.State::isSearching)

    val cardsNotFound: Flow<Boolean> = combine(
        searcherState.flowOf(CardsSearcher.State::searchText),
        isSearching,
        searcherState.flowOf(CardsSearcher.State::searchResult)
    ) { searchText: String, isSearching: Boolean, foundCards: List<FoundCard> ->
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