package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.AllCardsAreEmpty
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.FailureCause.HasUnderfilledCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForDeckCreation
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.DeckEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupScreenState
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem

class CardsEditorController(
    private val cardsEditor: CardsEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsEditorProvider: ShortTermStateProvider<CardsEditor>
) : BaseController<CardsEditorEvent, Command>() {
    sealed class Command {
        class ShowUnfilledTextInputAt(val position: Int) : Command()
        class ShowLevelOfKnowledgePopup(val intervalItems: List<IntervalItem>) : Command()
        object ShowIntervalsAreOffMessage : Command()
        object ShowCardIsRemovedMessage : Command()
        object AskUserToConfirmExit : Command()
    }

    override fun handle(event: CardsEditorEvent) {
        when (event) {
            is PageSelected -> {
                cardsEditor.setCurrentPosition(event.position)
            }

            LevelOfKnowledgeButtonClicked -> {
                onLevelOfKnowledgeButtonClicked()
            }

            is LevelOfKnowledgeSelected -> {
                cardsEditor.setLevelOfKnowledge(event.levelOfKnowledge)
            }

            NotAskButtonClicked -> {
                cardsEditor.setIsLearned(true)
            }

            AskAgainButtonClicked -> {
                cardsEditor.setIsLearned(false)
            }

            RemoveCardButtonClicked -> {
                val success = cardsEditor.removeCard()
                if (success) {
                    sendCommand(ShowCardIsRemovedMessage)
                }
            }

            RestoreLastRemovedCardButtonClicked -> {
                cardsEditor.restoreLastRemovedCard()
            }

            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            DoneButtonClicked -> {
                catchAndLogException {
                    when (val savingResult: SavingResult = cardsEditor.save()) {
                        Success -> {
                            when (cardsEditor) {
                                is CardsEditorForDeckCreation -> {
                                    navigator.navigateToDeckSetupFromCardsEditor {
                                        val deck = cardsEditor.createdDeck!!
                                        val screenState = DeckSetupScreenState(deck)
                                        val deckEditorState = DeckEditor.State(deck)
                                        DeckSetupDiScope.create(screenState, deckEditorState)
                                    }
                                }
                                else -> {
                                    navigator.navigateUp()
                                }
                            }
                        }
                        is Failure -> {
                            val problemPosition: Int = when (savingResult.failureCause) {
                                AllCardsAreEmpty -> cardsEditor.state.currentPosition
                                is HasUnderfilledCards -> savingResult.failureCause.positions[0]
                            }
                            sendCommand(ShowUnfilledTextInputAt(problemPosition))
                        }
                    }
                }
            }

            BackButtonClicked -> {
                if (cardsEditor.areCardsEdited()) {
                    sendCommand(AskUserToConfirmExit)
                } else {
                    navigator.navigateUp()
                }
            }

            UserConfirmedExit -> {
                navigator.navigateUp()
            }
        }
    }

    private fun onLevelOfKnowledgeButtonClicked() {
        val intervalScheme: IntervalScheme? =
            if (cardsEditor.currentEditableCard.deck == null) {
                ExercisePreference.Default.intervalScheme
            } else {
                cardsEditor.currentEditableCard.deck!!.exercisePreference.intervalScheme
            }
        if (intervalScheme == null) {
            sendCommand(ShowIntervalsAreOffMessage)
        } else {
            val currentLevelOfKnowledge: Int = cardsEditor.currentEditableCard.levelOfKnowledge
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
        cardsEditorProvider.save(cardsEditor)
    }
}