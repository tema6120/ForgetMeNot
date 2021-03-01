package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.searcher.CardsSearcher
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenState
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.export.ExportDiScope
import com.odnovolov.forgetmenot.presentation.screen.export.ExportDialogState
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope

class DeckContentController(
    private val screenState: DeckEditorScreenState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val screenStateProvider: ShortTermStateProvider<DeckEditorScreenState>
) : BaseController<DeckContentEvent, Nothing>() {
    override fun handle(event: DeckContentEvent) {
        when (event) {
            ExportButtonClicked -> {
                navigator.navigateToExportFromDeckEditor {
                    val dialogState = ExportDialogState(listOf(screenState.deck))
                    ExportDiScope.create(dialogState)
                }
            }

            SearchButtonClicked -> {
                navigator.navigateToSearchFromDeckEditor {
                    val cardsSearcher = CardsSearcher(screenState.deck)
                    SearchDiScope.create(cardsSearcher)
                }
            }

            is CardClicked -> {
                navigator.navigateToCardsEditorFromDeckEditor {
                    val deck = screenState.deck
                    val editableCards: List<EditableCard> =
                        deck.cards.map { card -> EditableCard(card, deck) }
                            .plus(EditableCard(Card(generateId(), "", ""), deck))
                    val position: Int = deck.cards.indexOfFirst { card -> card.id == event.cardId }
                    val cardsEditorState = State(editableCards, position)
                    val cardsEditor = CardsEditorForEditingDeck(
                        deck,
                        isNewDeck = false,
                        cardsEditorState,
                        globalState
                    )
                    CardsEditorDiScope.create(cardsEditor)
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        screenStateProvider.save(screenState)
    }
}