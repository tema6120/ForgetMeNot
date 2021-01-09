package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.*
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.domain.interactor.autoplay.Player
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.*

class SearchController(
    private val searcher: CardsSearcher,
    private val navigator: Navigator,
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
                            val cardsEditorState = State(editableCards, currentPosition)
                            val cardsEditor = CardsEditorForEditingExistingDeck(
                                deck,
                                cardsEditorState
                            )
                            CardsEditorDiScope.create(cardsEditor)
                        }
                    }
                    ExerciseDiScope.isOpen() -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val exercise: Exercise = ExerciseDiScope.getOrRecreate().exercise
                            val foundEditableCard = EditableCard(
                                event.searchCard.card,
                                event.searchCard.deck
                            )
                            val cardsEditorState = if (event.searchCard.card.id ==
                                exercise.currentExerciseCard.base.card.id
                            ) {
                                val editableCards: List<EditableCard> = listOf(foundEditableCard)
                                State(editableCards)
                            } else {
                                val editableCardFromExercise = EditableCard(
                                    exercise.currentExerciseCard.base.card,
                                    exercise.currentExerciseCard.base.deck
                                )
                                val editableCards: List<EditableCard> =
                                    listOf(editableCardFromExercise, foundEditableCard)
                                State(editableCards, currentPosition = 1)
                            }
                            val cardsEditor = CardsEditorForExercise(
                                exercise,
                                state = cardsEditorState
                            )
                            CardsEditorDiScope.create(cardsEditor)
                        }
                    }
                    PlayerDiScope.isOpen() -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val player: Player = PlayerDiScope.getOrRecreate().player
                            val foundEditableCard = EditableCard(
                                event.searchCard.card,
                                event.searchCard.deck
                            )
                            val cardsEditorState = if (event.searchCard.card.id ==
                                player.currentPlayingCard.card.id
                            ) {
                                val editableCards: List<EditableCard> = listOf(foundEditableCard)
                                State(editableCards)
                            } else {
                                val editableCardFromPlayer = EditableCard(
                                    player.currentPlayingCard.card,
                                    player.currentPlayingCard.deck
                                )
                                val editableCards: List<EditableCard> =
                                    listOf(editableCardFromPlayer, foundEditableCard)
                                State(editableCards, currentPosition = 1)
                            }
                            val cardsEditor = CardsEditorForAutoplay(
                                player,
                                state = cardsEditorState
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
                            val cardsEditorState = State(editableCards)
                            val cardsEditor = CardsEditorForEditingSpecificCards(
                                state = cardsEditorState
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