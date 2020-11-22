package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.soywiz.klock.DateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class HomeViewModel(
    homeScreenState: HomeScreenState,
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference,
    controller: HomeController,
    searcherState: CardsSearcher.State
) {
    private data class RawDeckPreview(
        val deckId: Long,
        val deckName: String,
        val createdAt: DateTime,
        val averageLaps: String,
        val learnedCount: Int,
        val totalCount: Int,
        val numberOfCardsReadyForExercise: Int?,
        val lastOpenedAt: String
    ) {
        fun toDeckPreview(searchMatchingRanges: List<IntRange>?) = DeckPreview(
            deckId,
            deckName,
            searchMatchingRanges,
            averageLaps,
            learnedCount,
            totalCount,
            numberOfCardsReadyForExercise,
            lastOpenedAt
        )
    }

    private val rawDecksPreview: Flow<List<RawDeckPreview>> = globalState.flowOf(GlobalState::decks)
        .map { decks: Collection<Deck> ->
            decks.map { deck: Deck ->
                val averageLaps: String = deck.cards
                    .map { it.lap }
                    .average()
                    .let { avgLaps: Double -> "%.1f".format(avgLaps) }
                val learnedCount = deck.cards.count { it.isLearned }
                val numberOfCardsReadyForExercise =
                    if (deck.exercisePreference.intervalScheme == null) {
                        null
                    } else {
                        deck.cards.count { card: Card ->
                            isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme)
                        }
                    }
                val lastOpenedAt: String = deck.lastOpenedAt?.format("MMM d") ?: "-"
                RawDeckPreview(
                    deckId = deck.id,
                    deckName = deck.name,
                    createdAt = deck.createdAt,
                    averageLaps = averageLaps,
                    learnedCount = learnedCount,
                    totalCount = deck.cards.size,
                    numberOfCardsReadyForExercise = numberOfCardsReadyForExercise,
                    lastOpenedAt = lastOpenedAt
                )
            }
        }
        .share()

    val deckSorting: Flow<DeckSorting> =
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting)

    private val sortedDecksPreview: Flow<List<RawDeckPreview>> = combine(
        rawDecksPreview,
        deckSorting
    ) { rawDecksPreview: List<RawDeckPreview>, deckSorting: DeckSorting ->
        when (deckSorting.direction) {
            Asc -> {
                when (deckSorting.criterion) {
                    Name -> rawDecksPreview.sortedBy { it.deckName }
                    CreatedAt -> rawDecksPreview.sortedBy { it.createdAt }
                    LastOpenedAt -> rawDecksPreview.sortedBy { it.lastOpenedAt }
                }
            }
            Desc -> {
                when (deckSorting.criterion) {
                    Name -> rawDecksPreview.sortedByDescending { it.deckName }
                    CreatedAt -> rawDecksPreview.sortedByDescending { it.createdAt }
                    LastOpenedAt -> rawDecksPreview.sortedByDescending { it.lastOpenedAt }
                }
            }
        }
    }
        .share()

    val displayOnlyDecksAvailableForExercise: Flow<Boolean> =
        deckReviewPreference.flowOf(DeckReviewPreference::displayOnlyDecksAvailableForExercise)

    private val searchText: Flow<String> = homeScreenState.flowOf(HomeScreenState::searchText)

    val decksPreview: Flow<List<DeckPreview>> = combine(
        sortedDecksPreview,
        displayOnlyDecksAvailableForExercise,
        searchText
    ) { sortedDecksPreview: List<RawDeckPreview>,
        displayOnlyWithTasks: Boolean,
        searchText: String
        ->
        if (searchText.isEmpty()) {
            sortedDecksPreview.run {
                if (displayOnlyWithTasks) {
                    filter { rawDeckPreview: RawDeckPreview ->
                        rawDeckPreview.numberOfCardsReadyForExercise == null
                                || rawDeckPreview.numberOfCardsReadyForExercise > 0
                    }
                } else {
                    this
                }
            }
                .map { rawDeckPreview: RawDeckPreview ->
                    rawDeckPreview.toDeckPreview(searchMatchingRanges = null)
                }
        } else {
            sortedDecksPreview
                .mapNotNull { rawDeckPreview: RawDeckPreview ->
                    val searchMatchingRanges: List<IntRange> =
                        findMatchingRange(rawDeckPreview.deckName, searchText)
                            ?: return@mapNotNull null
                    rawDeckPreview.toDeckPreview(searchMatchingRanges)
                }
        }
    }
        .share()
        .flowOn(Dispatchers.Default)

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

    val deckSelection: Flow<DeckSelection?> = homeScreenState.flowOf(HomeScreenState::deckSelection)
        .share()

    val hasSearchText: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::searchText)
            .map { it.isNotEmpty() }
            .distinctUntilChanged()

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
                if (!hasSearchText) {
                    add(DeckListItem.Header)
                }
                addAll(decksPreview)
                if (!hasSearchText || deckSelection != null) {
                    add(DeckListItem.Footer)
                }
            }
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    val numberOfSelectedCardsAvailableForExercise: Flow<Int?> = combine(
        decksPreview,
        deckSelection
    ) { decksPreview: List<DeckPreview>, deckSelection: DeckSelection? ->
        if (deckSelection == null || deckSelection.purpose == DeckSelection.Purpose.ForAutoplay) {
            null
        } else {
            decksPreview
                .filter { deckPreview -> deckPreview.deckId in deckSelection.selectedDeckIds }
                .map { deckPreview ->
                    with(deckPreview) {
                        numberOfCardsReadyForExercise ?: totalCount - learnedCount
                    }
                }
                .sum()
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    val decksNotFound: Flow<Boolean> = combine(
        hasSearchText,
        decksPreview
    ) { hasSearchText: Boolean, decksPreview: List<DeckPreview> ->
        hasSearchText && decksPreview.isEmpty()
    }
        .distinctUntilChanged()

    val areCardsBeingSearched: Flow<Boolean> =
        searcherState.flowOf(CardsSearcher.State::isSearching)

    val exerciseButtonState: Flow<ButtonState> = combine(
        deckSelection,
        hasSearchText
    ) { deckSelection: DeckSelection?, hasSearchText: Boolean ->
        if (deckSelection == null) {
            if (hasSearchText) {
                ButtonState.Invisible
            } else {
                ButtonState.Visible
            }
        } else {
            when {
                deckSelection.purpose == DeckSelection.Purpose.ForExercise
                        && deckSelection.selectedDeckIds.isEmpty() -> {
                    ButtonState.Inactive
                }
                deckSelection.purpose == DeckSelection.Purpose.ForExercise
                        || deckSelection.purpose == DeckSelection.Purpose.General -> {
                    ButtonState.Visible
                }
                else -> {
                    ButtonState.Invisible
                }
            }
        }
    }
        .distinctUntilChanged()

    val autoplayButtonState: Flow<ButtonState> = combine(
        deckSelection,
        hasSearchText
    ) { deckSelection: DeckSelection?, hasSearchText: Boolean ->
        if (deckSelection == null) {
            if (hasSearchText) {
                ButtonState.Invisible
            } else {
                ButtonState.Visible
            }
        } else {
            when {
                deckSelection.purpose == DeckSelection.Purpose.ForAutoplay
                        && deckSelection.selectedDeckIds.isEmpty() -> {
                    ButtonState.Inactive
                }
                deckSelection.purpose == DeckSelection.Purpose.ForAutoplay
                        || deckSelection.purpose == DeckSelection.Purpose.General -> {
                    ButtonState.Visible
                }
                else -> {
                    ButtonState.Invisible
                }
            }
        }
    }
        .distinctUntilChanged()

    val foundCards: Flow<List<SearchCard>> = searcherState.flowOf(CardsSearcher.State::searchResult)

    val cardsNotFound: Flow<Boolean> = combine(
        hasSearchText,
        areCardsBeingSearched,
        foundCards
    ) { hasSearchText: Boolean, areCardsBeingSearched: Boolean, foundCards: List<SearchCard> ->
        hasSearchText && !areCardsBeingSearched && foundCards.isEmpty()
    }
        .distinctUntilChanged()

    init {
        controller.displayedDeckIds = decksPreview.map { decksPreview: List<DeckPreview> ->
            decksPreview.map { it.deckId }
        }
    }
}