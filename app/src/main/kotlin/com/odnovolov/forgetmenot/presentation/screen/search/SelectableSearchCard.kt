package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

data class SelectableSearchCard(
    val card: Card,
    val deck: Deck,
    val questionMatchingRanges: List<IntRange>,
    val answerMatchingRanges: List<IntRange>,
    val isSelected: Boolean
)