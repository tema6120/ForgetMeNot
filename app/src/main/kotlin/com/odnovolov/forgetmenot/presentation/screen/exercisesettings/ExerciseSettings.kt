package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardFilterDisplay.WhenCardsMoreThan

class ExerciseSettings(
    cardFilterDisplay: CardFilterDisplay
) : FlowMakerWithRegistry<ExerciseSettings>() {
    var cardFilterDisplay: CardFilterDisplay by flowMaker(cardFilterDisplay)

    override fun copy() = ExerciseSettings(
        cardFilterDisplay
    )

    companion object {
        val DEFAULT_CARD_FILTER_DISPLAY = WhenCardsMoreThan(100)
    }
}

sealed class CardFilterDisplay {
    object Always : CardFilterDisplay()
    data class WhenCardsMoreThan(val numberOfCards: Int) : CardFilterDisplay()
    object Never : CardFilterDisplay()
}