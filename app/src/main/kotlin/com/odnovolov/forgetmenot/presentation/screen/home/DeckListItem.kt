package com.odnovolov.forgetmenot.presentation.screen.home

import com.soywiz.klock.DateTime

sealed class DeckListItem {
    object Header : DeckListItem()

    data class DeckPreview(
        val deckId: Long,
        val deckName: String,
        val searchMatchingRanges: List<IntRange>?,
        val averageLaps: Double,
        val learnedCount: Int,
        val totalCount: Int,
        val numberOfCardsReadyForExercise: Int?,
        val lastOpened: DateTime?,
        val isSelected: Boolean
    ) : DeckListItem()
}