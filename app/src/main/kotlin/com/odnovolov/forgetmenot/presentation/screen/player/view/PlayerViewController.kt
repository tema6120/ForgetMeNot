package com.odnovolov.forgetmenot.presentation.screen.player.view

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.BatchCardEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.AudioFocusManager
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
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerServiceController
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.ShowCannotGetAudioFocusMessage
import com.odnovolov.forgetmenot.presentation.screen.player.view.laps.LapsInPlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.view.laps.LapsInPlayerDialogState
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerViewController(
    private val player: Player,
    private val audioFocusManager: AudioFocusManager,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val screenState: PlayerScreenState,
    private val longTermStateSaver: LongTermStateSaver,
    private val playerStateProvider: ShortTermStateProvider<Player.State>,
    private val screenStateProvider: ShortTermStateProvider<PlayerScreenState>
) : BaseController<PlayerFragmentEvent, Command>() {
    sealed class Command {
        class SetCurrentPosition(val position: Int) : Command()
        object ShowCannotGetAudioFocusMessage : Command()
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

    private val currentPlayingCard: PlayingCard?
        get() = with(player.state) {
            playingCards.getOrNull(currentPosition)
        }

    override fun handle(event: PlayerFragmentEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                player.setCurrentPosition(event.position)
            }

            GradeButtonClicked -> {
                player.pause()
                audioFocusManager.abandonRequest(PlayerServiceController.AUDIO_FOCUS_KEY)
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
                val currentPlayingCard: PlayingCard = currentPlayingCard ?: return
                player.pause()
                screenState.wereDeckSettingsEdited = true
                navigator.navigateToDeckEditorFromPlayer {
                    val deck: Deck = currentPlayingCard.deck
                    val tabs = DeckEditorTabs.OnlyDeckSettings
                    val screenState = DeckEditorScreenState(deck, tabs)
                    DeckEditorDiScope.create(screenState)
                }
            }

            EditCardButtonClicked -> {
                val currentPlayingCard: PlayingCard = currentPlayingCard ?: return
                player.pause()
                navigator.navigateToCardEditorFromPlayer {
                    val editableCard = EditableCard(
                        currentPlayingCard.card,
                        currentPlayingCard.deck
                    )
                    val editableCards = listOf(editableCard)
                    val cardsEditorState = CardsEditor.State(editableCards)
                    val cardsEditor = CardsEditorForEditingSpecificCards(
                        cardsEditorState,
                        globalState,
                        player = player
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }

            SearchButtonClicked -> {
                player.pause()
                navigator.navigateToSearchFromPlayer {
                    val initialSearchText = with(player.state) {
                        when {
                            questionSelection.isNotEmpty() -> questionSelection
                            answerSelection.isNotEmpty() -> answerSelection
                            else -> ""
                        }
                    }
                    val cardsSearcher = CardsSearcher(globalState)
                    val batchCardEditor = BatchCardEditor(
                        globalState,
                        player = player
                    )
                    SearchDiScope.create(cardsSearcher, batchCardEditor, initialSearchText)
                }
            }

            LapsButtonClicked -> {
                navigator.showLapsInPlayerDialog {
                    val isInfinite = globalState.numberOfLapsInPlayer == Int.MAX_VALUE
                    val numberOfLapsInput: String =
                        if (isInfinite) "1"
                        else globalState.numberOfLapsInPlayer.toString()
                    val dialogState = LapsInPlayerDialogState(isInfinite, numberOfLapsInput)
                    LapsInPlayerDiScope.create(dialogState)
                }
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
                val success = audioFocusManager.request(PlayerServiceController.AUDIO_FOCUS_KEY)
                if (success) {
                    player.resume()
                } else {
                    sendCommand(ShowCannotGetAudioFocusMessage)
                }
            }

            FragmentResumed -> {
                if (screenState.wereDeckSettingsEdited) {
                    player.notifyExercisePreferenceChanged()
                    screenState.wereDeckSettingsEdited = false
                }
            }

            PlayAgainButtonClicked -> {
                val success = audioFocusManager.request(PlayerServiceController.AUDIO_FOCUS_KEY)
                if (success) {
                    player.playOneMoreLap()
                } else {
                    sendCommand(ShowCannotGetAudioFocusMessage)
                }
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