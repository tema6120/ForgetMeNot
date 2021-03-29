package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForAnswer
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForQuestion
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeDialogEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeDialogEvent.TextChanged

class CardTextSizeController(
    private val cardAppearance: CardAppearance,
    private val screenState: CardAppearanceScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<CardAppearanceScreenState>
) : BaseController<CardTextSizeDialogEvent, Nothing>() {
    override fun handle(event: CardTextSizeDialogEvent) {
        when (event) {
            is TextChanged -> {
                screenState.textSizeDialogText = event.text
            }

            OkButtonClicked -> {
                val textSize = screenState.textSizeDialogText.toIntOrNull() ?: return
                if (textSize < 1) return
                when (screenState.textSizeDialogDestination) {
                    ForQuestion -> cardAppearance.questionTextSize = textSize
                    ForAnswer -> cardAppearance.answerTextSize = textSize
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