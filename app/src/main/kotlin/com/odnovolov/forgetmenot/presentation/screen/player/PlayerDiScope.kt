package com.odnovolov.forgetmenot.presentation.screen.player

import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.persistence.shortterm.PlayerStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayingCardAdapter
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class PlayerDiScope private constructor(
    initialPlayerState: Player.State? = null
) {
    private val playerStateProvider = PlayerStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val playerState: Player.State =
        initialPlayerState ?: playerStateProvider.load()

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow,
        initialLanguage = playerState.playingCards[0].run {
            val pronunciation = deck.exercisePreference.pronunciation
            if (isReverse)
                pronunciation.answerLanguage else
                pronunciation.questionLanguage
        }
    )

    val player = Player(
        playerState,
        speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val serviceController = PlayerServiceController(
        player,
        AppDiScope.get().longTermStateSaver,
        playerStateProvider
    )

    val serviceModel = PlayerServiceModel(
        playerState
    )

    val viewController = PlayerViewController(
        player,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        playerStateProvider
    )

    val viewModel = PlayerViewModel(
        playerState,
        speakerImpl
    )

    private val playingCardController = PlayingCardController(
        player,
        AppDiScope.get().longTermStateSaver,
        playerStateProvider
    )

    fun getPlayingCardAdapter(coroutineScope: CoroutineScope) = PlayingCardAdapter(
        coroutineScope,
        playingCardController
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
            PlayerDiScope(initialPlayerState)

        override fun recreateDiScope() = PlayerDiScope()

        override fun onCloseDiScope(diScope: PlayerDiScope) {
            with(diScope) {
                speakerImpl.shutdown()
                player.cancel()
                serviceController.dispose()
                viewController.dispose()
                playingCardController.dispose()
            }
        }
    }
}