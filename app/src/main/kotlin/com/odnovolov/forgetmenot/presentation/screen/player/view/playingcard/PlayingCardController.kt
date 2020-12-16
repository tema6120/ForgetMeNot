package com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardEvent.*

class PlayingCardController(
    private val player: Player,
    private val longTermStateSaver: LongTermStateSaver,
    private val playerStateProvider: ShortTermStateProvider<Player.State>
) : BaseController<PlayingCardEvent, Nothing>() {
    override val autoSave = false

    override fun handle(event: PlayingCardEvent) {
        when (event) {
            ShowQuestionButtonClicked -> {
                player.showQuestion()
                saveState()
            }

            ShowAnswerButtonClicked -> {
                player.showAnswer()
                saveState()
            }

            is QuestionTextSelectionChanged -> {
                player.setQuestionSelection(event.selection)
            }

            is AnswerTextSelectionChanged -> {
                player.setAnswerSelection(event.selection)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        playerStateProvider.save(player.state)
    }
}