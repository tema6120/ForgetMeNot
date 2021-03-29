package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController.Command.ShowTextSizeDialog
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceEvent.*
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForAnswer
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForQuestion

class CardAppearanceController(
    private val cardAppearance: CardAppearance,
    private val screenState: CardAppearanceScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<CardAppearanceScreenState>
) : BaseController<CardAppearanceEvent, Command>() {
    sealed class Command {
        object ShowTextSizeDialog : Command()
    }

    override fun handle(event: CardAppearanceEvent) {
        when (event) {
            AlignQuestionToEdgeButtonClicked -> {
                cardAppearance.questionTextAlignment = CardTextAlignment.Edge
            }

            AlignQuestionToCenterButtonClicked -> {
                cardAppearance.questionTextAlignment = CardTextAlignment.Center
            }

            QuestionTextSizeButtonClicked -> {
                screenState.textSizeDialogText = cardAppearance.questionTextSize.toString()
                screenState.textSizeDialogDestination = ForQuestion
                sendCommand(ShowTextSizeDialog)
            }

            AlignAnswerToEdgeButtonClicked -> {
                cardAppearance.answerTextAlignment = CardTextAlignment.Edge
            }

            AlignAnswerToCenterButtonClicked -> {
                cardAppearance.answerTextAlignment = CardTextAlignment.Center
            }

            AnswerTextSizeButtonClicked -> {
                screenState.textSizeDialogText = cardAppearance.answerTextSize.toString()
                screenState.textSizeDialogDestination = ForAnswer
                sendCommand(ShowTextSizeDialog)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}