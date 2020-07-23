package com.odnovolov.forgetmenot.presentation.screen.search

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingExistingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingSpecificCards
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForRepetition
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupDiScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.*
import kotlinx.coroutines.runBlocking

class SearchController(
    private val screenState: SearchScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val searchScreenStateProvider: ShortTermStateProvider<SearchScreenState>
) : BaseController<SearchEvent, Nothing>() {
    override fun handle(event: SearchEvent) {
        when (event) {
            BackButtonClicked -> {
                navigator.navigateUp()
            }

            is SearchTextChanged -> {
                screenState.searchText = event.text
            }

            is CardClicked -> {
                when {
                    DeckSetupDiScope.isOpen() -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val deck = DeckSetupDiScope.get().screenState.relevantDeck
                            val editableCards: List<EditableCard> =
                                deck.cards.map { card -> EditableCard(card) } + EditableCard()
                            val currentPosition: Int = deck.cards.indexOfFirst { card ->
                                card.id == event.searchCard.card.id
                            }
                            val cardsEditorState = State(editableCards, currentPosition)
                            val cardsEditor =
                                CardsEditorForEditingExistingDeck(deck, cardsEditorState)
                            CardsEditorDiScope.create(cardsEditor)
                        }
                    }
                    ExerciseDiScope.isOpen() -> {

                    }
                    RepetitionDiScope.isOpen() -> {
                        navigator.navigateToCardsEditorFromSearch {
                            val repetition: Repetition = RepetitionDiScope.get().repetition
                            val foundEditableCard = EditableCard(
                                event.searchCard.card,
                                event.searchCard.deck
                            )
                            val cardsEditorState = if (event.searchCard.card.id ==
                                repetition.currentRepetitionCard.card.id
                            ) {
                                val editableCards: List<EditableCard> = listOf(foundEditableCard)
                                State(editableCards)
                            } else {
                                val editableCardFromRepetition = EditableCard(
                                    repetition.currentRepetitionCard.card,
                                    repetition.currentRepetitionCard.deck
                                )
                                val editableCards: List<EditableCard> =
                                    listOf(editableCardFromRepetition, foundEditableCard)
                                State(editableCards, currentPosition = 1)
                            }
                            val cardsEditor = CardsEditorForRepetition(
                                repetition,
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
        searchScreenStateProvider.save(screenState)
    }
}