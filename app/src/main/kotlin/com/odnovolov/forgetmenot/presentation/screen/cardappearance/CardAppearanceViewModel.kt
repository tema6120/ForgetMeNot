package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import kotlinx.coroutines.flow.Flow

class CardAppearanceViewModel(
    cardAppearance: CardAppearance
) {
    val questionTextAlignment: Flow<CardTextAlignment> =
        cardAppearance.flowOf(CardAppearance::questionTextAlignment)

    val questionTextSize: Flow<Int> =
        cardAppearance.flowOf(CardAppearance::questionTextSize)

    val answerTextAlignment: Flow<CardTextAlignment> =
        cardAppearance.flowOf(CardAppearance::answerTextAlignment)

    val answerTextSize: Flow<Int> =
        cardAppearance.flowOf(CardAppearance::answerTextSize)
}