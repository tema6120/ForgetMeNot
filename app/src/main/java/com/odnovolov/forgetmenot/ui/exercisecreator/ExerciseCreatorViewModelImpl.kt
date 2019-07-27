package com.odnovolov.forgetmenot.ui.exercisecreator

import androidx.lifecycle.MutableLiveData
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.entity.ExerciseCard
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.*
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.Action.ExerciseCreated
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.Event.CreateExercise
import java.util.*

class ExerciseCreatorViewModelImpl(
    private val dao: ExerciseCreatorDao
) : ExerciseCreatorViewModel {

    private val isProcessing = MutableLiveData(false)

    override val state = State(
        isProcessing
    )

    private val actionSender = LiveEvent<Action>()
    override val action = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            is CreateExercise -> {
                if (isProcessing.value == true) {
                    return
                }
                isProcessing.value = true
                try {
                    val deck = event.deck
                    val exerciseCards: List<ExerciseCard> = deck.cards
                        .filter { card -> !card.isLearned }
                        .map { card -> ExerciseCard(card = card) }
                        .sortedBy { exerciseCard -> exerciseCard.card.lap }
                    if (exerciseCards.isNotEmpty()) {
                        dao.deleteAllExerciseCards()
                        dao.insertExerciseCards(exerciseCards)
                        dao.updateLastOpenedAt(Calendar.getInstance(), deck.id)
                        actionSender.send(ExerciseCreated)
                    }
                } finally {
                    isProcessing.value = false
                }
            }
        }
    }

}