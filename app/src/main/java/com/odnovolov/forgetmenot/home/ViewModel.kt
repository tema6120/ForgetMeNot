package com.odnovolov.forgetmenot.home

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToList
import com.odnovolov.forgetmenot.common.database.mapToOne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
}