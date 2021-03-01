package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.CardMoving
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Failure
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.SavingResult.Success
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
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
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToCopyCard
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToMoveCard
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab.Settings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorTabs.All
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle.AdviceOnCompilingDeck
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
        object ShowCardIsCopiedMessage : Command()
        class ShowCardInfo(val cardInfo: CardInfo) : Command()
        object AskUserToConfirmExit : Command()
    }

    private val currentEditableCard: EditableCard?
        get() = with(cardsEditor.state) {
            editableCards.getOrNull(currentPosition)
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

            CopyCardButtonClicked -> {
                navigator.navigateToDeckChooserFromCardsEditor {
                    val screenState = DeckChooserScreenState(purpose = ToCopyCard)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToCopyCardToIsSelected -> {
                val success = cardsEditor.copyTo(event.abstractDeck)
                if (success) {
                    sendCommand(ShowCardIsCopiedMessage, postponeIfNotActive = true)
                }
            }

            CancelLastCopyingButtonClicked -> {
                cardsEditor.cancelLastCopying()
            }

            CardInfoButtonClicked -> {
                val currentEditableCard: EditableCard = currentEditableCard ?: return
                val deckOfCurrentCard: Deck = cardsEditor.state.movements
                    .find { cardMoving: CardMoving ->
                        cardMoving.editableCard.card.id == currentEditableCard.card.id
                    }
                    ?.targetDeck
                    ?: currentEditableCard.deck
                val deckName: String = deckOfCurrentCard.name
                val numberOfTests: String = currentEditableCard.card.lap.toString()
                val timeOfLastTest: String = currentEditableCard.card.lastTestedAt
                    ?.local
                    ?.format("HH:mm MMM d yyyy") ?: "-"
                val cardInfo = CardInfo(deckName, numberOfTests, timeOfLastTest)
                sendCommand(ShowCardInfo(cardInfo))
            }

            HelpButtonClicked -> {
                navigator.navigateToHelpArticleFromCardsEditor {
                    val screenState = HelpArticleScreenState(AdviceOnCompilingDeck)
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
                                    val tabs = All(
                                        initialTab = Settings
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