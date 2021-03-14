package com.odnovolov.forgetmenot.presentation.screen.exampleplayer

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager.AudiofocusState
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command.ShowCannotGetAudioFocusMessage
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ExamplePlayerController(
    private val player: Player,
    private val audioFocusManager: AudioFocusManager,
    private val playerStateProvider: ShortTermStateProvider<Player.State>
) : BaseController<ExamplePlayerEvent, Command>() {
    sealed class Command {
        class SetCurrentPosition(val position: Int) : Command()
        object ShowCannotGetAudioFocusMessage : Command()
    }

    private var resumeOnFocusGain = false

    init {
        combineTransform(
            player.state.flowOf(Player.State::currentPosition),
            player.state.flowOf(Player.State::isPlaying)
        ) { position: Int, isPlaying: Boolean ->
            if (isPlaying) {
                emit(SetCurrentPosition(position))
            }
        }
            .onEach { sendCommand(it) }
            .launchIn(coroutineScope)
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
        player.state.flowOf(Player.State::isPlaying).observe(coroutineScope) { isPlaying: Boolean ->
            if (isPlaying) {
                val success = audioFocusManager.request(PlayerServiceController.AUDIO_FOCUS_KEY)
                if (!success) {
                    player.pause()
                }
            } else {
                if (!resumeOnFocusGain) {
                    audioFocusManager.abandonRequest(PlayerServiceController.AUDIO_FOCUS_KEY)
                }
            }
        }
    }

    override fun handle(event: ExamplePlayerEvent) {
        when (event) {
            BottomSheetExpanded -> {
                tryToResumePlayer()
            }

            BottomSheetCollapsed -> {
                player.pause()
            }

            is NewPageBecameSelected -> {
                player.setCurrentPosition(event.position)
            }

            SpeakButtonClicked -> {
                player.speak()
            }

            StopSpeakButtonClicked -> {
                player.stopSpeaking()
            }

            PauseButtonClicked -> {
                player.pause()
            }

            ResumeButtonClicked -> {
                tryToResumePlayer()
            }

            FragmentPaused -> {
                player.pause()
            }
        }
    }

    private fun tryToResumePlayer() {
        val success = audioFocusManager.request(PlayerServiceController.AUDIO_FOCUS_KEY)
        if (success) {
            player.resume()
        } else {
            sendCommand(ShowCannotGetAudioFocusMessage)
        }
    }

    override fun saveState() {
        playerStateProvider.save(player.state)
    }

    override fun dispose() {
        super.dispose()
        audioFocusManager.abandonRequest(PlayerServiceController.AUDIO_FOCUS_KEY)
    }
}