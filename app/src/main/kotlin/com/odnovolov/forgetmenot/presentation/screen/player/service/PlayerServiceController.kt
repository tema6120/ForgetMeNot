package com.odnovolov.forgetmenot.presentation.screen.player.service

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager.AudiofocusState
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController.Command.ShowCannotGetAudioFocusMessage
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceEvent.*

class PlayerServiceController(
    private val player: Player,
    private val audioFocusManager: AudioFocusManager,
    private val longTermStateSaver: LongTermStateSaver,
    private val playerStateProvider: ShortTermStateProvider<Player.State>
) : BaseController<PlayerServiceEvent, Command>() {
    sealed class Command {
        object ShowCannotGetAudioFocusMessage : Command()
    }

    private var resumeOnFocusGain = false

    init {
        player.state.flowOf(Player.State::isPlaying).observe(coroutineScope) { isPlaying: Boolean ->
            if (isPlaying) {
                val success = audioFocusManager.request(AUDIO_FOCUS_KEY)
                if (!success) {
                    player.pause()
                }
            } else {
                if (!resumeOnFocusGain) {
                    audioFocusManager.abandonRequest(AUDIO_FOCUS_KEY)
                }
            }
        }
        audioFocusManager.state.flowOf(AudioFocusManager.State::audioFocusState)
            .observe(coroutineScope) { audioFocusState: AudiofocusState ->
                when (audioFocusState) {
                    AudiofocusState.NONE -> {}
                    AudiofocusState.GAIN -> {
                        if (resumeOnFocusGain) {
                            resumeOnFocusGain = false
                            player.resume()
                        }
                    }
                    AudiofocusState.LOSS -> {
                        resumeOnFocusGain = false
                        player.pause()
                    }
                    AudiofocusState.LOSS_TRANSIENT -> {
                        resumeOnFocusGain = player.state.isPlaying
                        player.pause()
                    }
                }
            }
    }

    override fun handle(event: PlayerServiceEvent) {
        when (event) {
            PauseNotificationActionClicked -> {
                player.pause()
            }

            ResumeNotificationActionClicked -> {
                val success = audioFocusManager.request(AUDIO_FOCUS_KEY)
                if (success) {
                    player.resume()
                } else {
                    sendCommand(ShowCannotGetAudioFocusMessage)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        playerStateProvider.save(player.state)
    }

    override fun dispose() {
        super.dispose()
        audioFocusManager.abandonRequest(AUDIO_FOCUS_KEY)
    }

    companion object {
        const val AUDIO_FOCUS_KEY = "Player"
    }
}