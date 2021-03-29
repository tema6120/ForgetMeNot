package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textopacitydialog

import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination

class CardTexOpacityViewModel(
    val screenState: CardAppearanceScreenState
) {
    val destination: TextOpacityDialogDestination?
        get() = screenState.textOpacityDialogDestination

    val textOpacity: Float
        get() = screenState.textOpacityInDialog
}