package com.odnovolov.forgetmenot.presentation.screen.player

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.persistence.shortterm.PlayerStateProvider
import com.odnovolov.forgetmenot.persistence.shortterm.ReadyToUseSerializableStateProvider
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerScreenState
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardController
import kotlinx.coroutines.Job

class PlayerDiScope private constructor(
    initialPlayerState: Player.State? = null,
    initialScreenState: PlayerScreenState? = null
) {
    val playerStateProvider = PlayerStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState,
        key = "PlayerState"
    )

    val playerState: Player.State =
        initialPlayerState ?: playerStateProvider.load()

    private val screenStateProvider = ReadyToUseSerializableStateProvider(
        PlayerScreenState.serializer(),
        AppDiScope.get().json,
        AppDiScope.get().database,
        key = PlayerScreenState::class.qualifiedName!!
    )

    private val screenState: PlayerScreenState =
        initialScreenState ?: screenStateProvider.load()

    private val audioFocusManager = AppDiScope.get().audioFocusManager

    val player = Player(
        playerState,
        AppDiScope.get().globalState,
        AppDiScope.get().speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val serviceController = PlayerServiceController(
        player,
        audioFocusManager,
        AppDiScope.get().longTermStateSaver,
        playerStateProvider
    )

    val serviceModel = PlayerServiceModel(
        playerState
    )

    val viewController = PlayerViewController(
        player,
        audioFocusManager,
        AppDiScope.get().globalState,
        AppDiScope.get().navigator,
        screenState,
        AppDiScope.get().longTermStateSaver,
        playerStateProvider,
        screenStateProvider
    )

    val viewModel = PlayerViewModel(
        playerState,
        AppDiScope.get().speakerImpl,
        AppDiScope.get().globalState
    )

    val playingCardController = PlayingCardController(
        player,
        AppDiScope.get().longTermStateSaver,
        playerStateProvider
    )

    companion object : DiScopeManager<PlayerDiScope>() {
        var isServiceAlive = false
            set(value) {
                field = value
                updateState()
            }

        var isFragmentAlive = false
            set(value) {
                field = value
                updateState()
            }

        private fun updateState() {
            when {
                isServiceAlive || isFragmentAlive -> reopenIfClosed()
                !isServiceAlive && !isFragmentAlive -> close()
            }
        }

        fun create(initialPlayerState: Player.State) =
            PlayerDiScope(initialPlayerState, PlayerScreenState())

        override fun recreateDiScope() = PlayerDiScope()

        override fun onCloseDiScope(diScope: PlayerDiScope) {
            with(diScope) {
                player.dispose()
                serviceController.dispose()
                viewController.dispose()
                playingCardController.dispose()
            }
        }
    }
}