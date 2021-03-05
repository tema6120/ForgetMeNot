package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
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
    private val longTermStateSaver: LongTermStateSaver
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

    override fun handle(event: SearchEvent) {
        when (event) {
            is SearchTextChanged -> {
                searcher.search(event.text)
            }

            is CardClicked -> {
                val selectableSearchCard: SelectableSearchCard = event.selectableSearchCard
                if (hasSelection()) {
                    toggleCardSelection(selectableSearchCard)
                } else {
                    when {
                        DeckEditorDiScope.isOpen() -> {
                            navigateToCardEditorForEditingDeck(selectableSearchCard)
                        }
                        ExerciseDiScope.isOpen() -> {
                            navigateToCardEditorForExercise(selectableSearchCard)
                        }
                        PlayerDiScope.isOpen() -> {
                            navigateToCardEditorForPlayer(selectableSearchCard)
                        }
                        else -> {
                            navigateToCardEditorForEditingSpecificCard(selectableSearchCard)
                        }
                    }
                }
            }

            is CardLongClicked -> {
                toggleCardSelection(event.selectableSearchCard)
            }

            CancelledCardSelection -> {
                batchCardEditor.clearSelection()
            }

            SelectAllCardsButtonClicked -> {
                val allEditableCards: List<EditableCard> = searcher.state.searchResult
                    .map { searchCard: SearchCard ->
                        EditableCard(
                            searchCard.card,
                            searchCard.deck
                        )
                    }
                batchCardEditor.addCardsToSelection(allEditableCards)
            }

            InvertCardSelectionOptionSelected -> {
                val numberOfInvertedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.invert()
                sendCommand(ShowCardsAreInvertedMessage(numberOfInvertedCards))
            }

            ChangeGradeCardSelectionOptionSelected -> {
                navigator.showChangeGradeDialogFromSearch {
                    val dialogState = ChangeGradeDialogState(
                        gradeItems = determineGradeItems(),
                        caller = ChangeGradeCaller.Search
                    )
                    ChangeGradeDiScope.create(dialogState)
                }
            }

            is SelectedGrade -> {
                val numberOfAffectedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.changeGrade(event.grade)
                sendCommand(ShowGradeIsChangedMessage(event.grade, numberOfAffectedCards))
            }

            MarkAsLearnedCardSelectionOptionSelected -> {
                val numberOfMarkedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.markAsLearned()
                sendCommand(ShowCardsAreMarkedAsLearnedMessage(numberOfMarkedCards))
            }

            MarkAsUnlearnedCardSelectionOptionSelected -> {
                val numberOfMarkedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.markAsUnlearned()
                sendCommand(ShowCardsAreMarkedAsUnlearnedMessage(numberOfMarkedCards))
            }

            RemoveCardsCardSelectionOptionSelected -> {
                val numberOfRemovedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.remove()
                updateSearchResult()
                sendCommand(ShowCardsAreRemovedMessage(numberOfRemovedCards))
            }

            MoveCardSelectionOptionSelected -> {
                navigator.navigateToDeckChooserFromSearch {
                    val screenState = DeckChooserScreenState(purpose = ToMoveCardsInSearch)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToMoveCardsToIsSelected -> {
                val numberOfMovedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.moveTo(event.abstractDeck)
                val deckName: String = event.abstractDeck.name
                updateSearchResult()
                sendCommand(
                    command = ShowCardsAreMovedMessage(numberOfMovedCards, deckName),
                    postponeIfNotActive = true
                )
            }

            CopyCardSelectionOptionSelected -> {
                navigator.navigateToDeckChooserFromSearch {
                    val screenState = DeckChooserScreenState(purpose = ToCopyCardsInSearch)
                    DeckChooserDiScope.create(screenState)
                }
            }

            is DeckToCopyCardsToIsSelected -> {
                val numberOfCopiedCards: Int = batchCardEditor.state.selectedCards.size
                batchCardEditor.copyTo(event.abstractDeck)
                val deckName: String = event.abstractDeck.name
                updateSearchResult()
                sendCommand(
                    command = ShowCardsAreCopiedMessage(numberOfCopiedCards, deckName),
                    postponeIfNotActive = true
                )
            }

            CancelSnackbarButtonClicked -> {
                batchCardEditor.cancelLastAction()
                updateSearchResult()
            }
        }
    }

    private fun updateSearchResult() {
        searcher.search(searcher.state.searchText)
    }

    private fun determineGradeItems(): List<GradeItem> {
        var baseIntervalScheme: IntervalScheme? = null
        for (searchCard: SearchCard in searcher.state.searchResult) {
            val intervalScheme: IntervalScheme =
                searchCard.deck.exercisePreference.intervalScheme ?: continue
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

    private fun toggleCardSelection(selectableSearchCard: SelectableSearchCard) {
        val editableCard = EditableCard(selectableSearchCard.card, selectableSearchCard.deck)
        batchCardEditor.toggleSelected(editableCard)
    }

    private fun navigateToCardEditorForEditingDeck(selectableSearchCard: SelectableSearchCard) {
        navigator.navigateToCardsEditorFromSearch {
            val deck = DeckEditorDiScope.getOrRecreate().screenState.deck
            val editableCards: List<EditableCard> =
                deck.cards.map { card -> EditableCard(card, deck) }
                    .plus(EditableCard(Card(generateId(), "", ""), deck))
            val currentPosition: Int = deck.cards.indexOfFirst { card ->
                card.id == selectableSearchCard.card.id
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

    private fun navigateToCardEditorForExercise(selectableSearchCard: SelectableSearchCard) {
        val exercise: Exercise = ExerciseDiScope.getOrRecreate().exercise
        val currentExerciseCard: ExerciseCard = with(exercise.state) {
            exerciseCards.getOrNull(currentPosition)
        } ?: return
        navigator.navigateToCardsEditorFromSearch {
            val foundEditableCard = EditableCard(
                selectableSearchCard.card,
                selectableSearchCard.deck
            )
            val cardsEditorState = if (selectableSearchCard.card.id ==
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

    private fun navigateToCardEditorForPlayer(selectableSearchCard: SelectableSearchCard) {
        val player: Player = PlayerDiScope.getOrRecreate().player
        val currentPlayingCard: PlayingCard = with(player.state) {
            playingCards.getOrNull(currentPosition)
        } ?: return
        navigator.navigateToCardsEditorFromSearch {
            val foundEditableCard = EditableCard(
                selectableSearchCard.card,
                selectableSearchCard.deck
            )
            val cardsEditorState =
                if (selectableSearchCard.card.id == currentPlayingCard.card.id) {
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

    private fun navigateToCardEditorForEditingSpecificCard(
        selectableSearchCard: SelectableSearchCard
    ) {
        navigator.navigateToCardsEditorFromSearch {
            val editableCard = EditableCard(
                selectableSearchCard.card,
                selectableSearchCard.deck
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
    }
}