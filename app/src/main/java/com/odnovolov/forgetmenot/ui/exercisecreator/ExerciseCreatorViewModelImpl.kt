package com.odnovolov.forgetmenot.ui.exercisecreator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.entity.ExerciseCard
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.*
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.Action.NotifyParentViewThatExerciseIsCreated
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.Event.CreateExerciseWasRequested
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ExerciseCreatorViewModelImpl(
    private val dao: ExerciseCreatorDao
) : ViewModel(), ExerciseCreatorViewModel {

    class Factory(val dao: ExerciseCreatorDao) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ExerciseCreatorViewModelImpl(dao) as T
        }
    }

    private val isProcessing = MutableLiveData(false)

    override val state = State(
        isProcessing
    )

    private val actionSender = LiveEvent<Action>()
    override val action = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            is CreateExerciseWasRequested -> {
                if (isProcessing.value == true) {
                    return
                }
                viewModelScope.launch {
                    isProcessing.value = true
                    try {
                        val deck = withContext(IO) {
                            dao.getDeck(event.deckId)
                        }
                        val exerciseCards: List<ExerciseCard> = withContext(Default) {
                            val exerciseCards: MutableList<ExerciseCard> = deck.cards
                                .filter { card -> !card.isLearned }
                                .map { card -> ExerciseCard(card = card) }
                                .toMutableList()
                            if (deck.exercisePreference.randomOrder) {
                                exerciseCards.shuffle()
                            }
                            exerciseCards
                                .sortedBy { exerciseCard -> exerciseCard.card.lap }
                        }
                        if (exerciseCards.isNotEmpty()) {
                            withContext(IO) {
                                dao.deleteAllExerciseCards()
                                dao.insertExerciseCards(exerciseCards)
                                dao.setLastOpenedAt(Calendar.getInstance(), deck.id)
                            }
                            actionSender.send(NotifyParentViewThatExerciseIsCreated)
                        }
                    } finally {
                        isProcessing.value = false
                    }
                }
            }
        }
    }

}