package com.odnovolov.forgetmenot.presentation.screen.player.view

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerViewController(
    private val player: Player,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val screenState: PlayerScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val playerStateProvider: ShortTermStateProvider<Player.State>,
    private val screenStateProvider: ShortTermStateProvider<PlayerScreenState>
) : BaseController<PlayerFragmentEvent, Command>() {
    sealed class Command {
        class SetCurrentPosition(val position: Int) : Command()
    }

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
    }

    override fun handle(event: PlayerFragmentEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                player.setCurrentPosition(event.position)
            }

            GradeButtonClicked -> {
                player.pause()
            }

            is GradeWasChanged -> {
                player.setGrade(event.grade)
            }

            MarkAsLearnedButtonClicked -> {
                player.setIsCardLearned(true)
            }

            MarkAsUnlearnedButtonClicked -> {
                player.setIsCardLearned(false)
            }

            SpeakButtonClicked -> {
                player.speak()
            }

            StopSpeakButtonClicked -> {
                player.stopSpeaking()
            }

            EditDeckSettingsButtonClicked -> {
                player.pause()
                screenState.wereDeckSettingsEdited = true
                navigator.navigateToDeckEditorFromPlayer {
                    val deck: Deck = player.currentPlayingCard.deck
                    val tabs = DeckEditorTabs.OnlyDeckSettings
                    val screenState = DeckEditorScreenState(deck, tabs)
                    DeckEditorDiScope.create(screenState)
                }
            }

            EditCardContentButtonClicked -> {
                player.pause()
                navigator.navigateToCardEditorFromPlayer {
                    val editableCard = EditableCard(
                        player.currentPlayingCard.card,
                        player.currentPlayingCard.deck
                    )
                    val editableCards = listOf(editableCard)
                    val cardsEditorState = CardsEditor.State(editableCards)
                    val cardsEditor = CardsEditorForAutoplay(
                        player,
                        state = cardsEditorState
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }

            SearchButtonClicked -> {
                player.pause()
                navigator.navigateToSearchFromPlayer {
                    val searchText = with(player.state) {
                        when {
                            questionSelection.isNotEmpty() -> questionSelection
                            answerSelection.isNotEmpty() -> answerSelection
                            else -> ""
                        }
                    }
                    SearchDiScope(searchText)
                }
            }

            InfinitePlaybackSwitchToggled -> {
                val enabled = !globalState.isInfinitePlaybackEnabled
                player.setInfinitePlaybackEnabled(enabled)
            }

            HelpButtonClicked -> {
                player.pause()
                navigator.navigateToHelpArticleFromPlayer {
                    val screenState = HelpArticleScreenState(HelpArticle.AutoplayingCards)
                    HelpArticleDiScope.create(screenState)
                }
            }

            PauseButtonClicked -> {
                player.pause()
            }

            ResumeButtonClicked -> {
                player.resume()
            }

            FragmentResumed -> {
                if (screenState.wereDeckSettingsEdited) {
                    player.notifyExercisePreferenceChanged()
                    screenState.wereDeckSettingsEdited = false
                }
            }

            PlayAgainButtonClicked -> {
                player.playOneMoreLap()
            }

            EndButtonClicked -> {
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        playerStateProvider.save(player.state)
        screenStateProvider.save(screenState)
    }
}