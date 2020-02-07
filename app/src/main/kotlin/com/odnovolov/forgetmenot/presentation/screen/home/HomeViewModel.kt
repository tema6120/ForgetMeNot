package com.odnovolov.forgetmenot.presentation.screen.home

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.removedeck.RemoveDeckInteractor
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.soywiz.klock.DateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HomeViewModel(
    homeScreenState: HomeScreenState,
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference,
    removeDeckInteractor: RemoveDeckInteractor,
    store: Store
) : ViewModel() {
    val displayOnlyWithTasks: Flow<Boolean> = deckReviewPreference
        .flowOf(DeckReviewPreference::displayOnlyWithTasks)

    val decksPreview: Flow<List<DeckPreview>> = combine(
        globalState.flowOf(GlobalState::decks),
        homeScreenState.flowOf(HomeScreenState::searchText),
        homeScreenState.flowOf(HomeScreenState::selectedDeckIds),
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting),
        displayOnlyWithTasks
    ) { decks: List<Deck>,
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
    }.share()

    val deckSelectionCount: Flow<DeckSelectionCount?> =
        decksPreview.map { decksPreview: List<DeckPreview> ->
            val selectedDecks = decksPreview
                .filter { deckPreview -> deckPreview.isSelected }
            if (selectedDecks.isEmpty()) {
                null
            } else {
                val selectedCardsCount = selectedDecks.map { deckPreview ->
                    with(deckPreview) { numberOfCardsReadyForExercise ?: totalCount - learnedCount }
                }
                    .sum()
                val selectedDecksCount = selectedDecks.size
                DeckSelectionCount(selectedCardsCount, selectedDecksCount)
            }
        }

    private fun List<Deck>.filterBy(searchText: String): List<Deck> {
        return if (searchText.isEmpty()) this
        else this.filter { it.name.contains(searchText) }
    }

    private fun List<Deck>.sortBy(deckSorting: DeckSorting): List<Deck> {
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
        val now = DateTime.now()
        return map { deck: Deck ->
            val passedLaps: Int? = deck.cards
                .filter { !it.isLearned }
                .minBy { it.lap }
                ?.lap
            val learnedCount = deck.cards.count { it.isLearned }
            val numberOfCardsReadyForExercise =
                if (deck.exercisePreference.intervalScheme == null) {
                    null
                } else {
                    val intervals: List<Interval> =
                        deck.exercisePreference.intervalScheme!!.intervals
                    deck.cards.count { card: Card ->
                        when {
                            card.isLearned -> false
                            card.lastAnsweredAt == null -> true
                            else -> {
                                val interval: Interval = intervals.find {
                                    it.targetLevelOfKnowledge == card.levelOfKnowledge
                                } ?: intervals.maxBy { it.targetLevelOfKnowledge }!!
                                card.lastAnsweredAt!! + interval.value < now
                            }
                        }
                    }
                }
            val isSelected = deck.id in selectedDeckIds
            DeckPreview(
                deckId = deck.id,
                deckName = deck.name,
                passedLaps = passedLaps,
                learnedCount = learnedCount,
                totalCount = deck.cards.size,
                numberOfCardsReadyForExercise = numberOfCardsReadyForExercise,
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

    private val displayedDeckIds: Flow<List<Long>> =
        decksPreview.map { decksPreview: List<DeckPreview> ->
            decksPreview.map { it.deckId }
        }

    val controller = HomeController(
        homeScreenState,
        deckReviewPreference,
        displayedDeckIds,
        removeDeckInteractor,
        store
    )

    override fun onCleared() {
        controller.onViewModelCleared()
    }
}