package com.odnovolov.forgetmenot.presentation.screen.cardappearance.example

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardTextAlignment
import kotlinx.coroutines.flow.Flow

class CardAppearanceExampleViewModel(
    cardAppearance: CardAppearance,
    private val screenState: CardAppearanceScreenState
) {
    val exampleCards: List<Card>
        get() = screenState.exampleCards

    val questionTextAlignment: Flow<CardTextAlignment> =
        cardAppearance.flowOf(CardAppearance::questionTextAlignment)

    val questionTextSize: Flow<Int> =
        cardAppearance.flowOf(CardAppearance::questionTextSize)

    val answerTextAlignment: Flow<CardTextAlignment> =
        cardAppearance.flowOf(CardAppearance::answerTextAlignment)

    val answerTextSize: Flow<Int> =
        cardAppearance.flowOf(CardAppearance::answerTextSize)
}