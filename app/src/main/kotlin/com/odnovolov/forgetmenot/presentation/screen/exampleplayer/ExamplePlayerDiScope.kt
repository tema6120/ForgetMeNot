package com.odnovolov.forgetmenot.presentation.screen.exampleplayer

import com.odnovolov.forgetmenot.domain.interactor.autoplay.CardFilterForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayerStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.PlayerStateProvider
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardController
import kotlinx.coroutines.Job

class ExamplePlayerDiScope private constructor(
    isRecreated: Boolean
) {
    private val playerStateProvider by lazy {
        PlayerStateProvider(
            AppDiScope.get().json,
            AppDiScope.get().database,
            AppDiScope.get().globalState,
            key = "ExamplePlayerState"
        )
    }

    private val playerStateCreator by lazy {
        PlayerStateCreator(
            PlayerStateCreator.State(
                listOf(DeckSettingsDiScope.getOrRecreate().deckSettings.state.deck),
                CardFilterForAutoplay.IncludeAll
            )
        )
    }

    private val playerState: Player.State =
        if (isRecreated) {
            playerStateProvider.load()
        } else {
            playerStateCreator.create().apply { isPlaying = false }
        }

    val player = Player(
        playerState,
        AppDiScope.get().globalState,
        AppDiScope.get().speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val cardAppearance: CardAppearance = AppDiScope.get().cardAppearance

    val controller = ExamplePlayerController(
        player,
        AppDiScope.get().audioFocusManager,
        playerStateProvider
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

    companion object : DiScopeManager<ExamplePlayerDiScope>() {
        fun create() = ExamplePlayerDiScope(isRecreated = false)

        override fun recreateDiScope() = ExamplePlayerDiScope(isRecreated = true)

        override fun onCloseDiScope(diScope: ExamplePlayerDiScope) {
            with(diScope) {
                player.dispose()
                controller.dispose()
                playingCardController.dispose()
            }
        }
    }
}