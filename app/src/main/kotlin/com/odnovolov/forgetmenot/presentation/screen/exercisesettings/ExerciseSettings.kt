package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.CardPrefilterMode.ShowFilterWhenCardsMoreThan
import kotlinx.serialization.Serializable

class ExerciseSettings(
    cardPrefilterMode: CardPrefilterMode,
    showProgressBar: Boolean,
    showTextOfCardPosition: Boolean,
    vibrateOnWrongAnswer: Boolean,
    goToNextCardAfterMarkingAsLearned: Boolean
) : FlowMakerWithRegistry<ExerciseSettings>() {
    var cardPrefilterMode: CardPrefilterMode by flowMaker(cardPrefilterMode)
    var showProgressBar: Boolean by flowMaker(showProgressBar)
    var showTextOfCardPosition: Boolean by flowMaker(showTextOfCardPosition)
    var vibrateOnWrongAnswer: Boolean by flowMaker(vibrateOnWrongAnswer)
    var goToNextCardAfterMarkingAsLearned: Boolean by flowMaker(goToNextCardAfterMarkingAsLearned)

    override fun copy() = ExerciseSettings(
        cardPrefilterMode,
        showProgressBar,
        showTextOfCardPosition,
        vibrateOnWrongAnswer,
        goToNextCardAfterMarkingAsLearned
    )

    companion object {
        const val DEFAULT_CARD_NUMBER_LIMITATION = 100
        val DEFAULT_CARD_PREFILTER_MODE =
            ShowFilterWhenCardsMoreThan(DEFAULT_CARD_NUMBER_LIMITATION)
        const val DEFAULT_SHOW_PROGRESS_BAR = true
        const val DEFAULT_SHOW_TEXT_OF_CARD_POSITION = true
        const val DEFAULT_VIBRATE_ON_WRONG_ANSWER = true
        const val DEFAULT_GO_TO_NEXT_CARD_AFTER_MARKING_AS_LEARNED = false
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