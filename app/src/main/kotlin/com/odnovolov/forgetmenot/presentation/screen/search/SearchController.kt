package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.FoundCard
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeCaller
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeDiScope
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeDialogState
import com.odnovolov.forgetmenot.presentation.screen.changegrade.GradeItem
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToCopyCardsInSearch
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.ToMoveCardsInSearch
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchController.Command
import com.odnovolov.forgetmenot.presentation.screen.search.SearchController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.*

class SearchController(
    private val searcher: CardsSearcher,
    private val batchCardEditor: BatchCardEditor,
    private val navigator: Navigator,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver,
    private val batchCardEditorProvider: ShortTermStateProvider<BatchCardEditor>
) : BaseController<SearchEvent, Command>() {
    sealed class Command {
        class ShowCardsAreInvertedMessage(val numberOfInvertedCards: Int) : Command()
        class ShowGradeIsChangedMessage(val grade: Int, val numberOfAffectedCards: Int) : Command()
        class ShowCardsAreMarkedAsLearnedMessage(val numberOfMarkedCards: Int) : Command()
        class ShowCardsAreMarkedAsUnlearnedMessage(val numberOfMarkedCards: Int) : Command()
        class ShowCardsAreRemovedMessage(val numberOfRemovedCards: Int) : Command()
        class ShowCardsAreMovedMessage(
            val numberOfMovedCards: Int,
            val deckNameToWhichCardsWereMoved: String
        ) : Command()

        class ShowCardsAreCopiedMessage(
            val numberOfCopiedCards: Int,
            val deckNameToWhichCardsWereCopied: String
        ) : Command()
    }

    private var needToResearchOnCancel = false

    override fun handle(event: SearchEvent) {
        when (event) {
            is SearchTextChanged -> {
                if (searcher.state.searchText == event.text) return
                searcher.search(event.text)
            }

            is CardClicked -> {
                val foundCard: FoundCard = searcher.state.searchResult
                    .find { it.card.id == event.cardId } ?: return
                if (hasSelection()) {
                    toggleCardSelection(foundCard)
                } else {
                    when {
                        DeckEditorDiScope.isOpen() -> {
                            navigateToCardEditorForEditingDeck(foundCard)
                        }
                        ExerciseDiScope.isOpen() -> {
                            navigateToCardEditorForExercise(foundCard)
                        }
                        PlayerDiScope.isOpen() -> {
                            navigateToCardEditorForPlayer(foundCard)
                        }
                        else -> {
                            navigateToCardEditorForEditingSpecificCard(foundCard)
                        }
                    }
                }
            }

            is CardLongClicked -> {
                val foundCard: FoundCard = searcher.state.searchResult
                    .find { it.card.id == event.cardId } ?: return
                toggleCardSelection(foundCard)
            }

            CardSelectionWasCancelled -> {
                batchCardEditor.clearSelection()
            }

            SelectAllCardsButtonClicked -> {
                val allEditableCards: List<EditableCard> = searcher.state.searchResult
                    .map { foundCard: FoundCard -> EditableCard(foundCard.card, foundCard.deck) }
                batchCardEditor.addCardsToSelection(allEditableCards)
            }

            InvertCardSelectionOptionWasSelected -> {
                val numberOfInvertedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.invert()
                sendCommand(ShowCardsAreInvertedMessage(numberOfInvertedCards))
                needToResearchOnCancel = false
                saveCardEditorDependentState()
            }

            ChangeGradeCardSelectionOptionWasSelected -> {
                navigator.showChangeGradeDialogFromSearch {
                    val dialogState = ChangeGradeDialogState(
                        gradeItems = determineGradeItems(),
                        caller = ChangeGradeCaller.Search
                    )
                    ChangeGradeDiScope.create(dialogState)
                }
            }

            is GradeWasSelected -> {
                val numberOfAffectedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.changeGrade(event.grade)
                sendCommand(ShowGradeIsChangedMessage(event.grade, numberOfAffectedCards))
                needToResearchOnCancel = false
                saveCardEditorDependentState()
            }

            MarkAsLearnedCardSelectionOptionWasSelected -> {
                val numberOfMarkedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.markAsLearned()
                sendCommand(ShowCardsAreMarkedAsLearnedMessage(numberOfMarkedCards))
                needToResearchOnCancel = false
                saveCardEditorDependentState()
            }

            MarkAsUnlearnedCardSelectionOptionWasSelected -> {
                val numberOfMarkedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.markAsUnlearned()
                sendCommand(ShowCardsAreMarkedAsUnlearnedMessage(numberOfMarkedCards))
                needToResearchOnCancel = false
                saveCardEditorDependentState()
            }

            RemoveCardsCardSelectionOptionWasSelected -> {
                val numberOfRemovedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.remove()
                searcher.research()
                sendCommand(ShowCardsAreRemovedMessage(numberOfRemovedCards))
                needToResearchOnCancel = true
                saveCardEditorDependentState()
            }

            MoveCardSelectionOptionWasSelected -> {
                navigator.navigateToDeckChooserFromSearch {
                    val screenState = DeckChooserScreenState(purpose = ToMoveCardsInSearch)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToMoveCardsToWasSelected -> {
                val numberOfMovedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.moveTo(event.abstractDeck)
                val deckName: String = event.abstractDeck.name
                searcher.research()
                sendCommand(
                    command = ShowCardsAreMovedMessage(numberOfMovedCards, deckName),
                    postponeIfNotActive = true
                )
                needToResearchOnCancel = true
                saveCardEditorDependentState()
            }

            CopyCardSelectionOptionWasSelected -> {
                navigator.navigateToDeckChooserFromSearch {
                    val screenState = DeckChooserScreenState(purpose = ToCopyCardsInSearch)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToCopyCardsToWasSelected -> {
                val numberOfCopiedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.copyTo(event.abstractDeck)
                val deckName: String = event.abstractDeck.name
                searcher.research()
                sendCommand(
                    command = ShowCardsAreCopiedMessage(numberOfCopiedCards, deckName),
                    postponeIfNotActive = true
                )
                needToResearchOnCancel = true
            }

            CancelSnackbarButtonClicked -> {
                batchCardEditor.cancelLastAction()
                if (needToResearchOnCancel) {
                    needToResearchOnCancel = false
                    searcher.research()
                }
                saveCardEditorDependentState()
            }
        }
    }

    private fun saveCardEditorDependentState() {
        when {
            batchCardEditor.exercise != null -> {
                ExerciseDiScope.getOrRecreate().run {
                    exerciseStateProvider.save(exerciseState)
                }
            }
            batchCardEditor.player != null -> {
                PlayerDiScope.getOrRecreate().run {
                    playerStateProvider.save(playerState)
                }
            }
        }
    }

    private fun determineGradeItems(): List<GradeItem> {
        var baseIntervalScheme: IntervalScheme? = null
        for (foundCard: FoundCard in searcher.state.searchResult) {
            val intervalScheme: IntervalScheme =
                foundCard.deck.exercisePreference.intervalScheme ?: continue
            when {
                baseIntervalScheme == null -> {
                    baseIntervalScheme = intervalScheme
                }
                baseIntervalScheme.id != intervalScheme.id -> {
                    baseIntervalScheme = null
                    break
                }
            }
        }
        return baseIntervalScheme?.intervals?.map { interval: Interval ->
            GradeItem(
                grade = interval.grade,
                waitingPeriod = interval.value
            )
        } ?: listOf(
            GradeItem(0, null),
            GradeItem(1, null),
            GradeItem(2, null),
            GradeItem(3, null),
            GradeItem(4, null),
            GradeItem(5, null),
            GradeItem(6, null)
        )
    }

    private fun hasSelection(): Boolean = batchCardEditor.state.selectedCards.isNotEmpty()

    private fun toggleCardSelection(foundCard: FoundCard) {
        val editableCard = EditableCard(foundCard.card, foundCard.deck)
        batchCardEditor.toggleSelected(editableCard)
    }

    private fun navigateToCardEditorForEditingDeck(foundCard: FoundCard) {
        navigator.navigateToCardsEditorFromSearch {
            val deck = DeckEditorDiScope.getOrRecreate().screenState.deck
            val editableCards: List<EditableCard> =
                deck.cards.map { card -> EditableCard(card, deck) }
                    .plus(EditableCard(Card(generateId(), "", ""), deck))
            val currentPosition: Int = deck.cards.indexOfFirst { card ->
                card.id == foundCard.card.id
            }
            val cardsEditorState = CardsEditor.State(editableCards, currentPosition)
            val cardsEditor = CardsEditorForEditingDeck(
                deck,
                isNewDeck = false,
                cardsEditorState,
                globalState
            )
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    private fun navigateToCardEditorForExercise(foundCard: FoundCard) {
        val exercise: Exercise = ExerciseDiScope.getOrRecreate().exercise
        val currentExerciseCard: ExerciseCard = with(exercise.state) {
            exerciseCards.getOrNull(currentPosition)
        } ?: return
        navigator.navigateToCardsEditorFromSearch {
            val foundEditableCard = EditableCard(
                foundCard.card,
                foundCard.deck
            )
            val cardsEditorState = if (foundCard.card.id ==
                currentExerciseCard.base.card.id
            ) {
                val editableCards: List<EditableCard> = listOf(foundEditableCard)
                CardsEditor.State(editableCards)
            } else {
                val editableCardFromExercise = EditableCard(
                    currentExerciseCard.base.card,
                    currentExerciseCard.base.deck
                )
                val editableCards: List<EditableCard> =
                    listOf(editableCardFromExercise, foundEditableCard)
                CardsEditor.State(editableCards, currentPosition = 1)
            }
            val cardsEditor = CardsEditorForEditingSpecificCards(
                cardsEditorState,
                globalState,
                exercise
            )
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    private fun navigateToCardEditorForPlayer(foundCard: FoundCard) {
        val player: Player = PlayerDiScope.getOrRecreate().player
        val currentPlayingCard: PlayingCard = with(player.state) {
            playingCards.getOrNull(currentPosition)
        } ?: return
        navigator.navigateToCardsEditorFromSearch {
            val foundEditableCard = EditableCard(
                foundCard.card,
                foundCard.deck
            )
            val cardsEditorState =
                if (foundCard.card.id == currentPlayingCard.card.id) {
                    val editableCards: List<EditableCard> =
                        listOf(foundEditableCard)
                    CardsEditor.State(editableCards)
                } else {
                    val editableCardFromPlayer = EditableCard(
                        currentPlayingCard.card,
                        currentPlayingCard.deck
                    )
                    val editableCards: List<EditableCard> =
                        listOf(editableCardFromPlayer, foundEditableCard)
                    CardsEditor.State(editableCards, currentPosition = 1)
                }
            val cardsEditor = CardsEditorForEditingSpecificCards(
                cardsEditorState,
                globalState,
                player = player
            )
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    private fun navigateToCardEditorForEditingSpecificCard(foundCard: FoundCard) {
        navigator.navigateToCardsEditorFromSearch {
            val editableCard = EditableCard(
                foundCard.card,
                foundCard.deck
            )
            val editableCards: List<EditableCard> = listOf(editableCard)
            val cardsEditorState = CardsEditor.State(editableCards)
            val cardsEditor = CardsEditorForEditingSpecificCards(
                cardsEditorState,
                globalState
            )
            CardsEditorDiScope.create(cardsEditor)
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        batchCardEditorProvider.save(batchCardEditor)
    }
}