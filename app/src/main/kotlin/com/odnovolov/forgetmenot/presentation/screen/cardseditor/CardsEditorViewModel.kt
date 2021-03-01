package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForEditingDeck
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class CardsEditorViewModel(
    private val cardsEditor: CardsEditor
) {
    val cardIds: Flow<List<Long>> = cardsEditor.state.flowOf(CardsEditor.State::editableCards)
        .map { editableCards: List<EditableCard> ->
            editableCards.map { it.card.id }
        }

    val currentPosition: Int get() = cardsEditor.state.currentPosition

    private val currentEditableCard: Flow<EditableCard?> = combine(
        cardsEditor.state.flowOf(CardsEditor.State::editableCards),
        cardsEditor.state.flowOf(CardsEditor.State::currentPosition)
    ) { editableCards: List<EditableCard>, currentPosition: Int ->
        if (currentPosition !in 0..editableCards.lastIndex) null
        else editableCards[currentPosition]
    }
        .share()

    val hasCards: Flow<Boolean> = cardIds.map { it.isNotEmpty() }

    private val isEditingNewDeck: Boolean =
        cardsEditor is CardsEditorForEditingDeck && cardsEditor.isNewDeck

    val gradeOfCurrentCard: Flow<Int?> =
        if (isEditingNewDeck) {
            flowOf(null)
        } else {
            currentEditableCard.flatMapLatest { editableCard: EditableCard? ->
                editableCard?.flowOf(EditableCard::grade) ?: flowOf(null)
            }
        }

    val intervalItems: Flow<List<IntervalItem>?> =
        if (isEditingNewDeck) {
            flowOf(null)
        } else {
            currentEditableCard.flatMapLatest { editableCard: EditableCard? ->
                if (editableCard == null) {
                    flowOf(null)
                } else {
                    val intervalScheme: IntervalScheme? =
                        editableCard.deck.exercisePreference.intervalScheme
                    editableCard.flowOf(EditableCard::grade)
                        .map { currentGrade: Int ->
                            intervalScheme?.intervals
                                ?.map { interval: Interval ->
                                    IntervalItem(
                                        grade = interval.grade,
                                        waitingPeriod = interval.value,
                                        isSelected = currentGrade == interval.grade
                                    )
                                }
                        }
                }
            }
        }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    val isCurrentEditableCardLearned: Flow<Boolean?> =
        if (isEditingNewDeck) {
            flowOf(null)
        } else {
            currentEditableCard.flatMapLatest { editableCard: EditableCard? ->
                editableCard?.flowOf(EditableCard::isLearned) ?: flowOf(null)
            }
        }

    private val questionOrAnswerUpdate: Flow<Unit> =
        currentEditableCard.flatMapLatest { currentEditableCard: EditableCard? ->
        if (currentEditableCard == null) {
            flowOf(Unit)
        } else {
            combine(
                currentEditableCard.flowOf(EditableCard::question),
                currentEditableCard.flowOf(EditableCard::answer),
            ) { _, _ -> }
        }
    }

    val isCurrentCardMovable: Flow<Boolean> =
        if (isEditingNewDeck) {
            flowOf(false)
        } else {
            questionOrAnswerUpdate.map { cardsEditor.isCurrentCardMovable() }
        }

    val isCurrentCardRemovable: Flow<Boolean> =
        questionOrAnswerUpdate.map { cardsEditor.isCurrentCardRemovable() }

    val isHelpButtonVisible: Boolean get() = isEditingNewDeck
}