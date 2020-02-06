package com.odnovolov.forgetmenot.presentation.screen.home

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
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

    val decksPreview: Flow<List<DeckPreview>> = globalState.flowOf(GlobalState::decks)
        .combine(homeScreenState.flowOf(HomeScreenState::searchText)) { decks, searchText ->
            if (searchText.isEmpty()) decks
            else decks.filter { it.name.contains(searchText) }
        }
        .map { decks: List<Deck> ->
            val now = DateTime.now()
            decks
                .map { deck: Deck ->
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

    override fun onCleared() {
        koinScope.close()
    }
}