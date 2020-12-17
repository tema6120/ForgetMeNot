package com.odnovolov.forgetmenot.presentation.screen.player.view

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.ShowIntervalsPopup
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PlayerViewController(
    private val player: Player,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val playerStateProvider: ShortTermStateProvider<Player.State>
) : BaseController<PlayerFragmentEvent, Command>() {
    sealed class Command {
        class SetCurrentPosition(val position: Int) : Command()
        class ShowIntervalsPopup(val intervalItems: List<IntervalItem>?) : Command()
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
                onGradeButtonClicked()
            }

            is GradeWasChanged -> {
                player.setGrade(event.grade)
            }

            NotAskButtonClicked -> {
                player.setIsCardLearned(true)
            }

            AskAgainButtonClicked -> {
                player.setIsCardLearned(false)
            }

            SpeakButtonClicked -> {
                player.speak()
            }

            StopSpeakButtonClicked -> {
                player.stopSpeaking()
            }

            EditCardButtonClicked -> {
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

            PauseButtonClicked -> {
                player.pause()
            }

            ResumeButtonClicked -> {
                player.resume()
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
                navigator.navigateToHelpFromPlayer {
                    HelpDiScope(HelpArticle.Repetition)
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

    private fun onGradeButtonClicked() {
        player.pause()
        val currentGrade: Int = player.currentPlayingCard.card.grade
        val intervalItems: List<IntervalItem>? = player.currentPlayingCard.deck
            .exercisePreference.intervalScheme?.intervals
            ?.map { interval: Interval ->
                IntervalItem(
                    grade = interval.grade,
                    waitingPeriod = interval.value,
                    isSelected = currentGrade == interval.grade
                )
            }
        sendCommand(ShowIntervalsPopup(intervalItems))
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        playerStateProvider.save(player.state)
    }
}