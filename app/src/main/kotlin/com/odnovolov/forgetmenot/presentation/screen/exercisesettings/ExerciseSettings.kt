package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardPrefilterMode.ShowFilterWhenCardsMoreThan
import kotlinx.serialization.Serializable

class ExerciseSettings(
    cardPrefilterMode: CardPrefilterMode
) : FlowMakerWithRegistry<ExerciseSettings>() {
    var cardPrefilterMode: CardPrefilterMode by flowMaker(cardPrefilterMode)

    override fun copy() = ExerciseSettings(
        cardPrefilterMode
    )

    companion object {
        const val DEFAULT_CARD_NUMBER_LIMITATION = 100
        val DEFAULT_CARD_PREFILTER_MODE =
            ShowFilterWhenCardsMoreThan(DEFAULT_CARD_NUMBER_LIMITATION)
    }
}

@Serializable
sealed class CardPrefilterMode {
    @Serializable
    object DoNotFilter : CardPrefilterMode()

    @Serializable
    data class LimitCardsTo(val numberOfCards: Int) : CardPrefilterMode()

    @Serializable
    data class ShowFilterWhenCardsMoreThan(val numberOfCards: Int) : CardPrefilterMode()

    @Serializable
    object AlwaysShowFilter : CardPrefilterMode()
}