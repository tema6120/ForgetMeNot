package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class HomeViewModel(
    homeScreenState: HomeScreenState,
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference,
    controller: HomeController,
    searcherState: CardsSearcher.State
) {
    val hasSearchText: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::searchText)
            .map { it.isNotEmpty() }
            .distinctUntilChanged()

    val hasSelectedDecks: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::selectedDeckIds)
            .map { it.isNotEmpty() }
            .distinctUntilChanged()

    val displayOnlyWithTasks: Flow<Boolean> =
        deckReviewPreference.flowOf(DeckReviewPreference::displayOnlyWithTasks)

    val deckSorting: Flow<DeckSorting> =
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting)

    private val decksPreview: Flow<List<DeckPreview>> = combine(
        globalState.flowOf(GlobalState::decks),
        homeScreenState.flowOf(HomeScreenState::searchText),
        homeScreenState.flowOf(HomeScreenState::selectedDeckIds),
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting),
        displayOnlyWithTasks
    ) { decks: Collection<Deck>,
        searchText: String,
        selectedDeckIds: List<Long>,
        deckSorting: DeckSorting,
        displayOnlyWithTasks: Boolean
        ->
        decks
            .filterBy(searchText)
            .sortBy(deckSorting)
            .mapToDeckPreview(selectedDeckIds, searchText)
            .run { if (searchText.isEmpty()) filterBy(displayOnlyWithTasks) else this }
    }
        .flowOn(Dispatchers.Default)
        .share()

    private fun Collection<Deck>.filterBy(searchText: String): Collection<Deck> {
        return if (searchText.isEmpty()) this
        else this.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    private fun Collection<Deck>.sortBy(deckSorting: DeckSorting): List<Deck> {
        return when (deckSorting.direction) {
            Asc -> {
                when (deckSorting.criterion) {
                    Name -> sortedBy { it.name }
                    CreatedAt -> sortedBy { it.createdAt }
                    LastOpenedAt -> sortedBy { it.lastOpenedAt }
                }
            }
            Desc -> {
                when (deckSorting.criterion) {
                    Name -> sortedByDescending { it.name }
                    CreatedAt -> sortedByDescending { it.createdAt }
                    LastOpenedAt -> sortedByDescending { it.lastOpenedAt }
                }
            }
        }
    }

    private fun List<Deck>.mapToDeckPreview(
        selectedDeckIds: List<Long>,
        searchText: String
    ): List<DeckPreview> {
        return map { deck: Deck ->
            val searchMatchingRanges: List<IntRange>? = findMatchingRange(deck.name, searchText)
            val averageLaps: Double = deck.cards
                .map { it.lap }
                .average()
            val learnedCount = deck.cards.count { it.isLearned }
            val numberOfCardsReadyForExercise =
                if (deck.exercisePreference.intervalScheme == null) {
                    null
                } else {
                    deck.cards.count { card: Card ->
                        isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme)
                    }
                }
            val isSelected = deck.id in selectedDeckIds
            DeckPreview(
                deckId = deck.id,
                deckName = deck.name,
                searchMatchingRanges = searchMatchingRanges,
                averageLaps = averageLaps,
                learnedCount = learnedCount,
                totalCount = deck.cards.size,
                numberOfCardsReadyForExercise = numberOfCardsReadyForExercise,
                lastOpened = deck.lastOpenedAt,
                isSelected = isSelected
            )
        }
    }

    private fun findMatchingRange(source: String, search: String): List<IntRange>? {
        if (search.isEmpty()) return null
        var start = source.indexOf(search, ignoreCase = true)
        if (start < 0) return null
        val result = ArrayList<IntRange>()
        while (start >= 0) {
            val end = start + search.length
            result += start..end
            start = source.indexOf(search, startIndex = end, ignoreCase = true)
        }
        return result
    }

    private fun List<DeckPreview>.filterBy(displayOnlyWithTasks: Boolean): List<DeckPreview> {
        return if (displayOnlyWithTasks) {
            filter {
                it.numberOfCardsReadyForExercise == null || it.numberOfCardsReadyForExercise > 0
            }
        } else this
    }

    val deckSelectionCount: Flow<DeckSelectionCount> =
        decksPreview.map { decksPreview: List<DeckPreview> ->
            val selectedDecks = decksPreview.filter { deckPreview -> deckPreview.isSelected }
            if (selectedDecks.isEmpty()) {
                DeckSelectionCount(selectedCardsCount = 0, selectedDecksCount = 0)
            } else {
                val selectedCardsCount = selectedDecks.map { deckPreview ->
                    with(deckPreview) { numberOfCardsReadyForExercise ?: totalCount - learnedCount }
                }
                    .sum()
                val selectedDecksCount = selectedDecks.size
                DeckSelectionCount(selectedCardsCount, selectedDecksCount)
            }
        }

    @OptIn(ExperimentalStdlibApi::class)
    val deckListItem: Flow<List<DeckListItem>> = combine(
        decksPreview,
        hasSearchText
    ) { decksPreview: List<DeckPreview>, hasSearchText: Boolean ->
        if (hasSearchText) {
            decksPreview
        } else {
            buildList {
                add(DeckListItem.Header)
                addAll(decksPreview)
            }
        }
    }

    val decksNotFound: Flow<Boolean> = combine(
        hasSearchText,
        decksPreview
    ) { hasSearchText: Boolean, decksPreview: List<DeckPreview> ->
        hasSearchText && decksPreview.isEmpty()
    }

    val isSearching: Flow<Boolean> = searcherState.flowOf(CardsSearcher.State::isSearching)

    val foundCards: Flow<List<SearchCard>> = searcherState.flowOf(CardsSearcher.State::searchResult)

    val cardsNotFound: Flow<Boolean> = combine(
        hasSearchText,
        foundCards
    ) { hasSearchText: Boolean, foundCards: List<SearchCard> ->
        hasSearchText && foundCards.isEmpty()
    }

    init {
        controller.displayedDeckIds = decksPreview.map { decksPreview: List<DeckPreview> ->
            decksPreview.map { it.deckId }
        }
    }
}