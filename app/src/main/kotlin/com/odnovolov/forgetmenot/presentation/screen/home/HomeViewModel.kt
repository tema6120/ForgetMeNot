package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
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

    val deckSelection: Flow<DeckSelection?> = homeScreenState.flowOf(HomeScreenState::deckSelection)

    val displayOnlyWithTasks: Flow<Boolean> =
        deckReviewPreference.flowOf(DeckReviewPreference::displayOnlyWithTasks)

    val deckSorting: Flow<DeckSorting> =
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting)

    val decksPreview: Flow<List<DeckPreview>> = combine(
        globalState.flowOf(GlobalState::decks),
        homeScreenState.flowOf(HomeScreenState::searchText),
        homeScreenState.flowOf(HomeScreenState::deckSelection),
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting),
        displayOnlyWithTasks
    ) { decks: Collection<Deck>,
        searchText: String,
        deckSelection: DeckSelection?,
        deckSorting: DeckSorting,
        displayOnlyWithTasks: Boolean
        ->
        decks
            .filterBy(searchText)
            .sortBy(deckSorting)
            .mapToDeckPreview(deckSelection, searchText)
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
        deckSelection: DeckSelection?,
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
            val isSelected = deckSelection?.let { deckSelection: DeckSelection ->
                deck.id in deckSelection.selectedDeckIds
            }
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

    @OptIn(ExperimentalStdlibApi::class)
    val deckListItem: Flow<List<DeckListItem>> = combine(
        decksPreview,
        hasSearchText,
        deckSelection
    ) { decksPreview: List<DeckPreview>,
        hasSearchText: Boolean,
        deckSelection: DeckSelection?
        ->
        if (decksPreview.isEmpty()) {
            decksPreview
        } else {
            buildList {
                if (!hasSearchText && deckSelection == null) {
                    add(DeckListItem.Header)
                }
                addAll(decksPreview)
                if (!hasSearchText || deckSelection != null) {
                    add(DeckListItem.Footer)
                }
            }
        }
    }

    val numberOfSelectedCardsAvailableForExercise: Flow<Int?> = combine(
        decksPreview,
        deckSelection
    ) { decksPreview: List<DeckPreview>, deckSelection: DeckSelection? ->
        if (deckSelection == null || deckSelection.purpose == DeckSelection.Purpose.ForAutoplay) {
            null
        } else {
            decksPreview
                .filter { deckPreview -> deckPreview.isSelected == true }
                .map { deckPreview ->
                    with(deckPreview) {
                        numberOfCardsReadyForExercise ?: totalCount - learnedCount
                    }
                }
                .sum()
        }
    }

    val decksNotFound: Flow<Boolean> = combine(
        hasSearchText,
        decksPreview
    ) { hasSearchText: Boolean, decksPreview: List<DeckPreview> ->
        hasSearchText && decksPreview.isEmpty()
    }

    val areCardsBeingSearched: Flow<Boolean> =
        searcherState.flowOf(CardsSearcher.State::isSearching)

    val isExerciseButtonVisible: Flow<Boolean> = combine(
        deckSelection,
        hasSearchText
    ) { deckSelection: DeckSelection?, hasSearchText: Boolean ->
        if (deckSelection == null) {
            !hasSearchText
        } else {
            when (deckSelection.purpose) {
                DeckSelection.Purpose.General,
                DeckSelection.Purpose.ForExercise -> true
                else -> false
            }
        }
    }

    val isAutoplayButtonVisible: Flow<Boolean> = combine(
        deckSelection,
        hasSearchText
    ) { deckSelection: DeckSelection?, hasSearchText: Boolean ->
        if (deckSelection == null) {
            !hasSearchText
        } else {
            when (deckSelection.purpose) {
                DeckSelection.Purpose.General,
                DeckSelection.Purpose.ForAutoplay -> true
                else -> false
            }
        }
    }

    val foundCards: Flow<List<SearchCard>> = searcherState.flowOf(CardsSearcher.State::searchResult)

    val cardsNotFound: Flow<Boolean> = combine(
        hasSearchText,
        areCardsBeingSearched,
        foundCards
    ) { hasSearchText: Boolean, areCardsBeingSearched: Boolean, foundCards: List<SearchCard> ->
        hasSearchText && !areCardsBeingSearched && foundCards.isEmpty()
    }

    init {
        controller.displayedDeckIds = decksPreview.map { decksPreview: List<DeckPreview> ->
            decksPreview.map { it.deckId }
        }
    }
}