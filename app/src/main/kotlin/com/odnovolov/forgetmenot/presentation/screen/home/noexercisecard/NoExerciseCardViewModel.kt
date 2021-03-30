package com.odnovolov.forgetmenot.presentation.screen.home.noexercisecard

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.soywiz.klock.DateTime

class NoExerciseCardViewModel(
    private val screenState: HomeScreenState
) {
    val relatedDeck: Deck?
        get() = screenState.deckRelatedToNoExerciseCardDialog

    val timeWhenTheFirstCardWillBeAvailable: DateTime?
        get() = screenState.timeWhenTheFirstCardWillBeAvailable
}