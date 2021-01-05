package com.odnovolov.forgetmenot.presentation.screen.exampleplayer

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player.State
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerEvent.*
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ExamplePlayerController(
    private val player: Player,
    private val playerStateProvider: ShortTermStateProvider<State>
) : BaseController<ExamplePlayerEvent, Command>() {
    sealed class Command {
        class SetCurrentPosition(val position: Int) : Command()
    }

    init {
        combineTransform(
            player.state.flowOf(State::currentPosition),
            player.state.flowOf(State::isPlaying)
        ) { position: Int, isPlaying: Boolean ->
            if (isPlaying) {
                emit(SetCurrentPosition(position))
            }
        }
            .onEach { sendCommand(it) }
            .launchIn(coroutineScope)
    }

    override fun handle(event: ExamplePlayerEvent) {
        when (event) {
            BottomSheetExpanded -> {
                player.resume()
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
                player.resume()
            }

            FragmentPaused -> {
                player.pause()
            }
        }
    }

    override fun saveState() {
        playerStateProvider.save(player.state)
    }
}