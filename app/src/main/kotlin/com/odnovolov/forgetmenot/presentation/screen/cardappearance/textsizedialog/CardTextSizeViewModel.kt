package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog

import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CardTextSizeViewModel(
    private val screenState: CardAppearanceScreenState
) {
    val destination: TextSizeDialogDestination?
        get() = screenState.textSizeDialogDestination

    val dialogText: String
        get() = screenState.textSizeDialogText

    val isOkButtonEnabled: Flow<Boolean> =
        screenState.flowOf(CardAppearanceScreenState::textSizeDialogText)
            .map { textSizeText: String -> textSizeText.toIntOrNull()?.let { it > 0 } ?: false }
}