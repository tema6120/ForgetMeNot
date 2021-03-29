package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController.Command.ShowTextOpacityDialog
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController.Command.ShowTextSizeDialog
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceEvent.*
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination.ForDarkTheme
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextOpacityDialogDestination.ForLightTheme
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForAnswer
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForQuestion
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardTextAlignment.Center
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardTextAlignment.Edge

class CardAppearanceController(
    private val cardAppearance: CardAppearance,
    private val screenState: CardAppearanceScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<CardAppearanceScreenState>
) : BaseController<CardAppearanceEvent, Command>() {
    sealed class Command {
        object ShowTextSizeDialog : Command()
        object ShowTextOpacityDialog : Command()
    }

    override fun handle(event: CardAppearanceEvent) {
        when (event) {
            AlignQuestionToEdgeButtonClicked -> {
                cardAppearance.questionTextAlignment = Edge
            }

            AlignQuestionToCenterButtonClicked -> {
                cardAppearance.questionTextAlignment = Center
            }

            QuestionTextSizeButtonClicked -> {
                screenState.textSizeDialogText = cardAppearance.questionTextSize.toString()
                screenState.textSizeDialogDestination = ForQuestion
                sendCommand(ShowTextSizeDialog)
            }

            AlignAnswerToEdgeButtonClicked -> {
                cardAppearance.answerTextAlignment = Edge
            }

            AlignAnswerToCenterButtonClicked -> {
                cardAppearance.answerTextAlignment = Center
            }

            AnswerTextSizeButtonClicked -> {
                screenState.textSizeDialogText = cardAppearance.answerTextSize.toString()
                screenState.textSizeDialogDestination = ForAnswer
                sendCommand(ShowTextSizeDialog)
            }

            TextOpacityInLightThemeButtonClicked -> {
                screenState.textOpacityInDialog = cardAppearance.textOpacityInLightTheme
                screenState.textOpacityDialogDestination = ForLightTheme
                sendCommand(ShowTextOpacityDialog)
            }

            TextOpacityInDarkThemeButtonClicked -> {
                screenState.textOpacityInDialog = cardAppearance.textOpacityInDarkTheme
                screenState.textOpacityDialogDestination = ForDarkTheme
                sendCommand(ShowTextOpacityDialog)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}