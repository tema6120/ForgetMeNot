package com.odnovolov.forgetmenot.screen.home

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToList
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.home.DeckPreview
import com.odnovolov.forgetmenot.home.HomeViewModelQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HomeViewModel {
    val queries: HomeViewModelQueries = database.homeViewModelQueries

    val displayOnlyWithTasks: Flow<Boolean> = queries.displayOnlyWithTasks().asFlow().mapToOne()

    val decksPreview: Flow<List<DeckPreview>> = queries
        .deckPreview()
        .asFlow()
        .mapToList()
        .combine(
            flow = displayOnlyWithTasks,
            transform = { decksPreview: List<DeckPreview>, displayOnlyWithTasks: Boolean ->
                if (displayOnlyWithTasks) {
                    decksPreview.filter {
                        when (it.numberOfCardsReadyForExercise) {
                            null -> true
                            0L -> false
                            else -> true
                        }
                    }
                } else {
                    decksPreview
                }
            })

    val hasAnySelectedDeck: Flow<Boolean> = decksPreview.map { decksPreview ->
        decksPreview.any { it.isSelected }
    }

    private val selectedDecksPreview = decksPreview.map { it.filter(DeckPreview::isSelected) }

    val selectedDecksCount: Flow<Int> = selectedDecksPreview.map { it.count() }

    val selectedCardsCount: Flow<Long> = selectedDecksPreview.map {
        it.map { deckPreview ->
            with (deckPreview) {
                numberOfCardsReadyForExercise ?: totalCount - learnedCount
            }
        }.sum()
    }
}