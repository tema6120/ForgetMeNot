package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class HomeViewModel(
    homeScreenState: HomeScreenState,
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference,
    controller: HomeController
) {
    val hasSelectedDecks: Flow<Boolean> =
        homeScreenState.flowOf(HomeScreenState::selectedDeckIds).map { it.isNotEmpty() }

    val displayOnlyWithTasks: Flow<Boolean> =
        deckReviewPreference.flowOf(DeckReviewPreference::displayOnlyWithTasks)

    val decksPreview: Flow<List<DeckPreview>> = combine(
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
            .mapToDeckPreview(selectedDeckIds)
            .filterBy(displayOnlyWithTasks)
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

    private fun List<Deck>.mapToDeckPreview(selectedDeckIds: List<Long>): List<DeckPreview> {
        return map { deck: Deck ->
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
                averageLaps = averageLaps,
                learnedCount = learnedCount,
                totalCount = deck.cards.size,
                numberOfCardsReadyForExercise = numberOfCardsReadyForExercise,
                lastOpened = deck.lastOpenedAt,
                isSelected = isSelected
            )
        }
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

    init {
        controller.displayedDeckIds = decksPreview.map { decksPreview: List<DeckPreview> ->
            decksPreview.map { it.deckId }
        }
    }
}