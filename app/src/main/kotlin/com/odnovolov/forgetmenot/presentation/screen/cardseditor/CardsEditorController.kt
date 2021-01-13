package com.odnovolov.forgetmenot.presentation.screen.cardseditor

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
import com.odnovolov.forgetmenot.presentation.common.doWithCatchingExceptions
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleScreenState

class CardsEditorController(
    private val cardsEditor: CardsEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsEditorProvider: ShortTermStateProvider<CardsEditor>
) : BaseController<CardsEditorEvent, Command>() {
    sealed class Command {
        class ShowUnfilledTextInputAt(val position: Int) : Command()
        object ShowCardIsRemovedMessage : Command()
        object AskUserToConfirmExit : Command()
    }

    override fun handle(event: CardsEditorEvent) {
        when (event) {
            is PageSelected -> {
                cardsEditor.setCurrentPosition(event.position)
            }

            is GradeWasChanged -> {
                cardsEditor.setGrade(event.grade)
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

            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromCardsEditor {
                    val screenState = HelpArticleScreenState(HelpArticle.AdviceOnCompilingDeck)
                    HelpArticleDiScope.create(screenState)
                }
            }

            CancelButtonClicked -> {
                if (cardsEditor.areCardsEdited()) {
                    sendCommand(AskUserToConfirmExit)
                } else {
                    navigator.navigateUp()
                }
            }

            DoneButtonClicked, SaveButtonClicked -> {
                doWithCatchingExceptions {
                    when (val savingResult: SavingResult = cardsEditor.save()) {
                        Success -> {
                            when (cardsEditor) {
                                is CardsEditorForDeckCreation -> {
                                    navigator.navigateToDeckSetupFromCardsEditor {
                                        val deck = cardsEditor.createdDeck!!
                                        val screenState = DeckEditorScreenState(deck)
                                        val deckEditorState = DeckEditor.State(deck)
                                        DeckEditorDiScope.create(screenState, deckEditorState)
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

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsEditorProvider.save(cardsEditor)
    }
}