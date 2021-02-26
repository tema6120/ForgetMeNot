package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.DeckPreview
import com.soywiz.klock.DateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class HomeViewModel(
    homeScreenState: HomeScreenState,
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference,
    controller: HomeController,
    searcherState: CardsSearcher.State
) {
    data class RawDeckPreview(
        val deckId: Long,
        val deckName: String,
        val createdAt: DateTime,
        val averageLaps: Double,
        val learnedCount: Int,
        val totalCount: Int,
        val numberOfCardsReadyForExercise: Int?,
        val lastTestedAt: DateTime?,
        val isPinned: Boolean
    ) {
        fun toDeckPreview(searchMatchingRanges: List<IntRange>?) = DeckPreview(
            deckId,
            deckName,
            searchMatchingRanges,
            "%.1f".format(averageLaps),
            learnedCount,
            totalCount,
            numberOfCardsReadyForExercise,
            lastTestedAt?.format("MMM d"),
            isPinned
        )
    }

    private val fiveSeconds: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(5 * 1000)
        }
    }

    private val rawDecksPreview: Flow<List<RawDeckPreview>> = globalState.flowOf(GlobalState::decks)
        .flatMapLatest { decks: Collection<Deck> ->
            if (decks.isEmpty()) {
                flowOf(emptyList())
            } else {
                val flowsForUpdating: MutableList<Flow<Unit>> = ArrayList(decks.size * 2)
                for (deck in decks) {
                    flowsForUpdating.add(deck.flowOf(Deck::name).map {  })
                    flowsForUpdating.add(deck.flowOf(Deck::isPinned).map {  })
                }
                combine(flowsForUpdating) { decks }.debounce(10)
            }
        }
        .combine(fiveSeconds) { decks: Collection<Deck>, _ -> decks }
        .map { decks: Collection<Deck> ->
            decks.map { deck: Deck ->
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
                RawDeckPreview(
                    deckId = deck.id,
                    deckName = deck.name,
                    createdAt = deck.createdAt,
                    averageLaps = averageLaps,
                    learnedCount = learnedCount,
                    totalCount = deck.cards.size,
                    numberOfCardsReadyForExercise = numberOfCardsReadyForExercise,
                    lastTestedAt = deck.lastTestedAt,
                    isPinned = deck.isPinned
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
        val comparator = DeckPreviewComparator(deckSorting)
        rawDecksPreview.sortedWith(comparator)
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

    val numberOfSelectedDecks: Flow<Int> = deckSelection.map { deckSelection: DeckSelection? ->
        deckSelection?.selectedDeckIds?.size ?: 0
    }

    val hasSearchText: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::searchText)
            .map { it.isNotEmpty() }
            .distinctUntilChanged()

    @OptIn(ExperimentalStdlibApi::class)
    val deckListItems: Flow<List<DeckListItem>> = combine(
        decksPreview,
        hasSearchText,
        deckSelection
    ) { decksPreview: List<DeckPreview>,
        hasSearchText: Boolean,
        deckSelection: DeckSelection?
        ->
        if (decksPreview.isEmpty()) {
            listOf(DeckListItem.Header)
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

    val deckNameInDeckOptionMenu: Flow<String?> =
        homeScreenState.flowOf(HomeScreenState::deckForDeckOptionMenu)
            .map { deck: Deck? -> deck?.name }

    val isDeckInDeckOptionPinned: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::deckForDeckOptionMenu)
            .map { deck: Deck? -> deck?.isPinned ?: false }

    val isPinDeckSelectionOptionAvailable: Flow<Boolean> = combine(
        deckSelection,
        decksPreview
    ) { deckSelection: DeckSelection?, decksPreview: List<DeckPreview> ->
        if (deckSelection == null) {
            false
        } else {
            decksPreview.any { deckPreview: DeckPreview ->
                deckPreview.deckId in deckSelection.selectedDeckIds && !deckPreview.isPinned
            }
        }
    }

    val isUnpinDeckSelectionOptionAvailable: Flow<Boolean> = combine(
        deckSelection,
        decksPreview
    ) { deckSelection: DeckSelection?, decksPreview: List<DeckPreview> ->
        if (deckSelection == null) {
            false
        } else {
            decksPreview.any { deckPreview: DeckPreview ->
                deckPreview.deckId in deckSelection.selectedDeckIds && deckPreview.isPinned
            }
        }
    }

    val decksNotFound: Flow<Boolean> = combine(
        hasSearchText,
        decksPreview
    ) { hasSearchText: Boolean, decksPreview: List<DeckPreview> ->
        hasSearchText && decksPreview.isEmpty()
    }
        .distinctUntilChanged()

    val hasDecks: Flow<Boolean> = combine(
        hasSearchText,
        decksPreview
    ) { hasSearchText: Boolean, decksPreview: List<DeckPreview> ->
        decksPreview.isNotEmpty() || hasSearchText
    }
        .distinctUntilChanged()

    val areCardsBeingSearched: Flow<Boolean> =
        searcherState.flowOf(CardsSearcher.State::isSearching)

    val isExerciseButtonVisible: Flow<Boolean> = combine(
        hasDecks,
        deckSelection,
        hasSearchText
    ) { hasDecks: Boolean, deckSelection: DeckSelection?, hasSearchText: Boolean ->
        when {
            !hasDecks -> false
            deckSelection == null -> !hasSearchText
            else -> {
                deckSelection.selectedDeckIds.isNotEmpty() &&
                        (deckSelection.purpose == DeckSelection.Purpose.ForExercise ||
                                deckSelection.purpose == DeckSelection.Purpose.General)
            }
        }
    }
        .distinctUntilChanged()

    val isAutoplayButtonVisible: Flow<Boolean> = combine(
        hasDecks,
        deckSelection,
        hasSearchText
    ) { hasDecks: Boolean, deckSelection: DeckSelection?, hasSearchText: Boolean ->
        when {
            !hasDecks -> false
            deckSelection == null -> !hasSearchText
            else -> {
                deckSelection.selectedDeckIds.isNotEmpty() &&
                        (deckSelection.purpose == DeckSelection.Purpose.ForAutoplay ||
                                deckSelection.purpose == DeckSelection.Purpose.General)
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

    val areFilesBeingReading: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::areFilesBeingReading)

    init {
        controller.displayedDeckIds = decksPreview.map { decksPreview: List<DeckPreview> ->
            decksPreview.map { it.deckId }
        }
    }
}