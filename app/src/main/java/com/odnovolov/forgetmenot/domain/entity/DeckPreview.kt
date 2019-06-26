package com.odnovolov.forgetmenot.domain.entity

data class DeckPreview(
    val deckId: Int,
    val deckName: String,
    val passedLaps: Int,
    val progress: Progress
) {
    data class Progress(
        val learned: Int,
        val total: Int
    ) {
        override fun toString(): String {
            return "$learned/$total"
        }
    }
}