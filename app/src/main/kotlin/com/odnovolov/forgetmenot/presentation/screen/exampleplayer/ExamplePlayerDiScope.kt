package com.odnovolov.forgetmenot.presentation.screen.exampleplayer

import com.odnovolov.forgetmenot.domain.entity.CardFilterForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayerStateCreator
import com.odnovolov.forgetmenot.persistence.shortterm.PlayerStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardController
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class ExamplePlayerDiScope private constructor(
    isRecreated: Boolean
) {
    private val playerStateProvider by lazy {
        PlayerStateProvider(
            AppDiScope.get().json,
            AppDiScope.get().database,
            AppDiScope.get().globalState
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

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app,
        AppDiScope.get().activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow,
        initialLanguage = playerState.playingCards[0].run {
            val pronunciation = deck.exercisePreference.pronunciation
            if (isInverted)
                pronunciation.answerLanguage else
                pronunciation.questionLanguage
        }
    )

    val player = Player(
        playerState,
        AppDiScope.get().globalState,
        speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val controller = ExamplePlayerController(
        player,
        playerStateProvider
    )

    val viewModel = PlayerViewModel(
        playerState,
        speakerImpl,
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
                speakerImpl.shutdown()
                player.cancel()
                controller.dispose()
                playingCardController.dispose()
            }
        }
    }
}