package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import kotlinx.coroutines.flow.Flow

class CardAppearanceViewModel(
    private val cardAppearance: CardAppearance
) {
    val questionTextAlignment: Flow<CardTextAlignment> =
        cardAppearance.flowOf(CardAppearance::questionTextAlignment)

    val questionTextSize: String
        get() = cardAppearance.questionTextSize.toString()

    val answerTextAlignment: Flow<CardTextAlignment> =
        cardAppearance.flowOf(CardAppearance::answerTextAlignment)

    val answerTextSize: String
        get() = cardAppearance.answerTextSize.toString()
}