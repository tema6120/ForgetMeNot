package com.odnovolov.forgetmenot.presentation.screen.home

data class DeckPreview(
    val deckId: Long,
    val deckName: String,
    val passedLaps: Int?,
    val learnedCount: Int,
    val totalCount: Int,
    val numberOfCardsReadyForExercise: Int?,
    val isSelected: Boolean
)