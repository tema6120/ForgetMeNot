package com.odnovolov.forgetmenot.presentation.screen.cardinversion

import com.odnovolov.forgetmenot.domain.entity.CardInversion
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionEvent.*

class CardInversionController(
    private val deckSettings: DeckSettings,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<CardInversionEvent, Nothing>() {
    override fun handle(event: CardInversionEvent) {
        when (event) {
            OffRadioButtonClicked -> {
                deckSettings.setCardInversion(CardInversion.Off)
            }

            OnRadioButtonClicked -> {
                deckSettings.setCardInversion(CardInversion.On)
            }

            EveryOtherLapRadioButtonClicked -> {
                deckSettings.setCardInversion(CardInversion.EveryOtherLap)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}