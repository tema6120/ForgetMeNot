package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.CardClicked
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.SearchTextChanged

class SearchController(
    private val searcher: CardsSearcher,
    private val navigator: Navigator,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<SearchEvent, Nothing>() {
    override fun handle(event: SearchEvent) {
        when (event) {
            is SearchTextChanged -> {
                searcher.search(event.text)
            }

            is CardClicked -> {
                when {
                    DeckEditorDiScope.isOpen() -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val deck = DeckEditorDiScope.getOrRecreate().screenState.deck
                            val editableCards: List<EditableCard> =
                                deck.cards.map { card -> EditableCard(card, deck) }
                                    .plus(EditableCard(Card(generateId(), "", ""), deck))
                            val currentPosition: Int = deck.cards.indexOfFirst { card ->
                                card.id == event.searchCard.card.id
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
                    ExerciseDiScope.isOpen() -> {
                        val exercise: Exercise = ExerciseDiScope.getOrRecreate().exercise
                        val currentExerciseCard: ExerciseCard = with(exercise.state) {
                            exerciseCards.getOrNull(currentPosition)
                        } ?: return
                        navigator.navigateToCardsEditorFromSearch {
                            val foundEditableCard = EditableCard(
                                event.searchCard.card,
                                event.searchCard.deck
                            )
                            val cardsEditorState = if (event.searchCard.card.id ==
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
                    PlayerDiScope.isOpen() -> {
                        val player: Player = PlayerDiScope.getOrRecreate().player
                        val currentPlayingCard: PlayingCard = with(player.state) {
                            playingCards.getOrNull(currentPosition)
                        } ?: return
                        navigator.navigateToCardsEditorFromSearch {
                            val foundEditableCard = EditableCard(
                                event.searchCard.card,
                                event.searchCard.deck
                            )
                            val cardsEditorState =
                                if (event.searchCard.card.id ==
                                    currentPlayingCard.card.id
                                ) {
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
                    else -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val editableCard = EditableCard(
                                event.searchCard.card,
                                event.searchCard.deck
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
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}