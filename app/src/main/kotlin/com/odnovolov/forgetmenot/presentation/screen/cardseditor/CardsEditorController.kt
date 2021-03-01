package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.doWithCatchingExceptions
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToMoveCard
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs
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
        object ShowCardIsMovedMessage : Command()
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

            MarkAsLearnedButtonClicked -> {
                cardsEditor.setIsLearned(true)
            }

            MarkAsUnlearnedButtonClicked -> {
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

            MoveCardButtonClicked -> {
                navigator.navigateToDeckChooserFromCardsEditor {
                    val screenState = DeckChooserScreenState(purpose = ToMoveCard)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToMoveCardToIsSelected -> {
                val success = cardsEditor.moveTo(event.abstractDeck)
                if (success) {
                    sendCommand(ShowCardIsMovedMessage, postponeIfNotActive = true)
                }
            }

            CancelLastMovementButtonClicked -> {
                cardsEditor.cancelLastMovement()
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
                            if (cardsEditor is CardsEditorForEditingDeck && cardsEditor.isNewDeck) {
                                navigator.navigateToDeckEditorFromCardsEditor {
                                    val tabs = DeckEditorTabs.All(
                                        initialTab = DeckEditorScreenTab.Settings
                                    )
                                    val screenState = DeckEditorScreenState(cardsEditor.deck, tabs)
                                    DeckEditorDiScope.create(screenState)
                                }
                            } else {
                                navigator.navigateUp()
                            }
                        }
                        is Failure -> {
                            val problemPosition: Int = savingResult.underfilledPositions[0]
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