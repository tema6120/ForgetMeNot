package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination.ForDarkTheme
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination.ForLightTheme
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog.CardTexOpacityDialogEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog.CardTexOpacityDialogEvent.TextOpacityWasSelected

class CardTexOpacityController(
    private val cardAppearance: CardAppearance,
    private val screenState: CardAppearanceScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<CardAppearanceScreenState>
) : BaseController<CardTexOpacityDialogEvent, Nothing>() {
    private val textOpacityRange = 0.01f..1.0f

    override fun handle(event: CardTexOpacityDialogEvent) {
        when (event) {
            is TextOpacityWasSelected -> {
                if (event.textOpacity !in textOpacityRange) return
                screenState.textOpacityInDialog = event.textOpacity
            }

            OkButtonClicked -> {
                val textOpacity: Float = screenState.textOpacityInDialog
                if (textOpacity !in textOpacityRange) return
                when (screenState.textOpacityDialogDestination) {
                    ForLightTheme -> cardAppearance.textOpacityInLightTheme = textOpacity
                    ForDarkTheme -> cardAppearance.textOpacityInDarkTheme = textOpacity
                    null -> return
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}