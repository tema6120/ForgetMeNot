package com.odnovolov.forgetmenot.presentation.screen.search

data class SelectableSearchCard(
    val cardId: Long,
    val question: String,
    val answer: String,
    val isLearned: Boolean,
    val grade: Int,
    val searchText: String,
    val isSelected: Boolean
)