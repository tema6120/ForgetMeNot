package com.odnovolov.forgetmenot.presentation.screen.home

import com.soywiz.klock.DateTime

data class DeckPreview(
    val deckId: Long,
    val deckName: String,
    val averageLaps: Double,
    val learnedCount: Int,
    val totalCount: Int,
    val numberOfCardsReadyForExercise: Int?,
    val lastOpened: DateTime?,
    val isSelected: Boolean
)