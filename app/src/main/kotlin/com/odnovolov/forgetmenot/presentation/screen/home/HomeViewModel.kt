package com.odnovolov.forgetmenot.presentation.screen.home

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.soywiz.klock.DateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.koin.ext.getOrCreateScope

class HomeViewModel(
    globalState: GlobalState,
    deckReviewPreference: DeckReviewPreference
) : ViewModel() {
    private val koinScope = getOrCreateScope()
    val controller: HomeController = koinScope.get()
    private val homeScreenState: HomeScreenState = koinScope.get()

    val displayOnlyWithTasks: Flow<Boolean> = deckReviewPreference
        .flowOf(DeckReviewPreference::displayOnlyWithTasks)

    val decksPreview: Flow<List<DeckPreview>> = combine(
        globalState.flowOf(GlobalState::decks),
        homeScreenState.flowOf(HomeScreenState::searchText),
        deckReviewPreference.flowOf(DeckReviewPreference::deckSorting),
        deckReviewPreference.flowOf(DeckReviewPreference::displayOnlyWithTasks)
    ) { decks: List<Deck>,
        searchText: String,
        deckSorting: DeckSorting,
        displayOnlyWithTasks: Boolean
        ->
        decks
            .filterBy(searchText)
            .sortBy(deckSorting)
            .mapToDeckPreview()
            .filterBy(displayOnlyWithTasks)
    }

    private val deckSelection: Flow<List<Long>> =
        homeScreenState.flowOf(HomeScreenState::selectedDeckIds)

    val hasAnySelectedDeck: Flow<Boolean> = deckSelection.map { it.isNotEmpty() }

    val selectedDecksCount: Flow<Int> = deckSelection.map { it.size }

    val selectedCardsCount: Flow<Int> = deckSelection.combine(decksPreview)
    { selectedDeckIds: List<Long>, decksPreview: List<DeckPreview> ->
        decksPreview
            .filter { deckPreview -> selectedDeckIds.contains(deckPreview.deckId) }
            .map { deckPreview ->
                with(deckPreview) {
                    numberOfCardsReadyForExercise ?: totalCount - learnedCount
                }
            }
            .sum()
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

    private fun List<Deck>.mapToDeckPreview(): List<DeckPreview> {
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
            DeckPreview(
                deckId = deck.id,
                deckName = deck.name,
                passedLaps = passedLaps,
                learnedCount = learnedCount,
                totalCount = deck.cards.size,
                numberOfCardsReadyForExercise = numberOfCardsReadyForExercise,
                isSelected = false // todo
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

    override fun onCleared() {
        koinScope.close()
    }
}