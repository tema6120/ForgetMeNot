package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.Card

data class SearchCard(
    val card: Card,
    val questionMatchingRanges: List<IntRange>,
    val answerMatchingRanges: List<IntRange>
)