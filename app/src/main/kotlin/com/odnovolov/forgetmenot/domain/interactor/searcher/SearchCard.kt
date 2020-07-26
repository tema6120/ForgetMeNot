package com.odnovolov.forgetmenot.domain.interactor.searcher

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck

data class SearchCard(
    val card: Card,
    val deck: Deck,
    val questionMatchingRanges: List<IntRange>,
    val answerMatchingRanges: List<IntRange>
)