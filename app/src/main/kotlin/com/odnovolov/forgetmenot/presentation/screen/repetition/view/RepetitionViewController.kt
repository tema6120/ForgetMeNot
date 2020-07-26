package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForRepetition
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RepetitionViewController(
    private val repetition: Repetition,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionStateProvider: ShortTermStateProvider<Repetition.State>
) : BaseController<RepetitionFragmentEvent, Command>() {
    sealed class Command {
        class SetViewPagerPosition(val position: Int) : Command()
        class ShowLevelOfKnowledgePopup(val intervalItems: List<IntervalItem>) : Command()
        object ShowIntervalsAreOffMessage : Command()
    }

    init {
        combineTransform(
            repetition.state.flowOf(Repetition.State::repetitionCardPosition),
            repetition.state.flowOf(Repetition.State::isPlaying)
        ) { position: Int, isPlaying: Boolean ->
            if (isPlaying) {
                emit(SetViewPagerPosition(position))
            }
        }
            .onEach { sendCommand(it) }
            .launchIn(coroutineScope)
    }

    override fun handle(event: RepetitionFragmentEvent) {
        when (event) {
            is NewPageBecameSelected -> {
                repetition.setRepetitionCardPosition(event.position)
            }

            LevelOfKnowledgeButtonClicked -> {
                onLevelOfKnowledgeButtonClicked()
            }

            is LevelOfKnowledgeSelected -> {
                repetition.setLevelOfKnowledge(event.levelOfKnowledge)
            }

            NotAskButtonClicked -> {
                repetition.setIsCardLearned(true)
            }

            AskAgainButtonClicked -> {
                repetition.setIsCardLearned(false)
            }

            SpeakButtonClicked -> {
                repetition.speak()
            }

            StopSpeakButtonClicked -> {
                repetition.stopSpeaking()
            }

            EditCardButtonClicked -> {
                repetition.pause()
                navigator.navigateToCardEditorFromRepetition {
                    val editableCard = EditableCard(
                        repetition.currentRepetitionCard.card,
                        repetition.currentRepetitionCard.deck
                    )
                    val editableCards = listOf(editableCard)
                    val cardsEditorState = CardsEditor.State(editableCards)
                    val cardsEditor = CardsEditorForRepetition(
                        repetition,
                        state = cardsEditorState
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }

            PauseButtonClicked -> {
                repetition.pause()
            }

            ResumeButtonClicked -> {
                repetition.resume()
            }

            SearchButtonClicked -> {
                repetition.pause()
                navigator.navigateToSearchFromRepetition {
                    val searchText = with(repetition.state) {
                        when {
                            questionSelection.isNotEmpty() -> questionSelection
                            answerSelection.isNotEmpty() -> answerSelection
                            else -> ""
                        }
                    }
                    SearchDiScope(searchText)
                }
            }
        }
    }

    private fun onLevelOfKnowledgeButtonClicked() {
        repetition.pause()
        val intervalScheme: IntervalScheme? =
            repetition.currentRepetitionCard.deck.exercisePreference.intervalScheme
        if (intervalScheme == null) {
            sendCommand(ShowIntervalsAreOffMessage)
        } else {
            val currentLevelOfKnowledge: Int =
                repetition.currentRepetitionCard.card.levelOfKnowledge
            val intervalItems: List<IntervalItem> = intervalScheme.intervals
                .map { interval: Interval ->
                    IntervalItem(
                        levelOfKnowledge = interval.levelOfKnowledge,
                        waitingPeriod = interval.value,
                        isSelected = currentLevelOfKnowledge == interval.levelOfKnowledge
                    )
                }
            sendCommand(ShowLevelOfKnowledgePopup(intervalItems))
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        repetitionStateProvider.save(repetition.state)
    }
}