package com.odnovolov.forgetmenot.presentation.screen.player.service

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceEvent.PauseNotificationActionClicked
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceEvent.ResumeNotificationActionClicked

class PlayerServiceController(
    private val player: Player,
    private val longTermStateSaver: LongTermStateSaver,
    private val playerStateProvider: ShortTermStateProvider<Player.State>
) : BaseController<PlayerServiceEvent, Nothing>() {
    override fun handle(event: PlayerServiceEvent) {
        when (event) {
            PauseNotificationActionClicked -> {
                player.pause()
            }

            ResumeNotificationActionClicked -> {
                player.resume()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        playerStateProvider.save(player.state)
    }
}