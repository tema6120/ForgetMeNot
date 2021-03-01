package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.*
import com.odnovolov.forgetmenot.presentation.screen.home.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel.RawDeckPreview
import kotlinx.coroutines.flow.*

class DeckChooserViewModel(
    private val screenState: DeckChooserScreenState,
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference
) {
    val purpose: DeckChooserScreenState.Purpose get() = screenState.purpose

    private val searchText: Flow<String> = screenState.flowOf(DeckChooserScreenState::searchText)

    private val hasSearchText: Flow<Boolean> = searchText.map { it.isNotEmpty() }
        .distinctUntilChanged()

    private val rawDecksPreview: Flow<List<RawDeckPreview>> = globalState.flowOf(GlobalState::decks)
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

    private val decksPreview: Flow<List<DeckPreview>> = combine(
        sortedDecksPreview,
        searchText
    ) { sortedDecksPreview: List<RawDeckPreview>,
        searchText: String
        ->
        if (searchText.isEmpty()) {
            sortedDecksPreview
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
        .flowOn(businessLogicThread)

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

    @OptIn(ExperimentalStdlibApi::class)
    val deckListItems: Flow<List<DeckListItem>> = combine(
        decksPreview,
        hasSearchText
    ) { decksPreview: List<DeckPreview>,
        hasSearchText: Boolean
        ->
        if (hasSearchText) {
            decksPreview
        } else {
            listOf(Header) + decksPreview
        }
    }
        .distinctUntilChanged()
        .flowOn(businessLogicThread)

    val decksNotFound: Flow<Boolean> = combine(
        hasSearchText,
        decksPreview
    ) { hasSearchText: Boolean, decksPreview: List<DeckPreview> ->
        hasSearchText && decksPreview.isEmpty()
    }
        .distinctUntilChanged()

    val isAddDeckButtonVisible: Boolean
        get() = when (screenState.purpose) {
            ToMergeInto, ToMoveCard, ToCopyCard -> true
            else -> false
        }
}