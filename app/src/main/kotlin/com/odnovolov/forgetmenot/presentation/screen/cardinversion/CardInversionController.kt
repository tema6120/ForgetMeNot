package com.odnovolov.forgetmenot.presentation.screen.cardinversion

import com.odnovolov.forgetmenot.domain.entity.CardInversion
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExampleExercise
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionEvent.*

class CardInversionController(
    private val deckSettings: DeckSettings,
    private val exercise: ExampleExercise,
    private val screenState: CardInversionScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<CardInversionScreenState>
) : BaseController<CardInversionEvent, Nothing>() {
    override fun handle(event: CardInversionEvent) {
        when (event) {
            CloseTipButtonClicked -> {
                screenState.tip?.state?.needToShow = false
                screenState.tip = null
            }

            OffRadioButtonClicked -> {
                deckSettings.setCardInversion(CardInversion.Off)
                exercise.notifyExercisePreferenceChanged()
            }

            OnRadioButtonClicked -> {
                deckSettings.setCardInversion(CardInversion.On)
                exercise.notifyExercisePreferenceChanged()
            }

            EveryOtherLapRadioButtonClicked -> {
                deckSettings.setCardInversion(CardInversion.EveryOtherLap)
                exercise.notifyExercisePreferenceChanged()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}