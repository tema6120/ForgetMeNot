package com.odnovolov.forgetmenot.domain.interactor.prepareexercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.SUID
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.domain.interactor.prepareexercise.PrepareExerciseInteractor.Event.ExerciseIsReady
import com.odnovolov.forgetmenot.domain.interactor.prepareexercise.PrepareExerciseInteractor.Event.NoCardIsReadyForExercise
import com.soywiz.klock.DateTime
import kotlinx.coroutines.flow.Flow

class PrepareExerciseInteractor(
    private val globalState: GlobalState
) {
    sealed class Event {
        class ExerciseIsReady(val exerciseState: Exercise.State) : Event()
        object NoCardIsReadyForExercise : Event()
    }

    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()

    fun prepare(deckIds: List<Long>) {
        val now = DateTime.now()
        val exerciseCards: List<ExerciseCard> = globalState.decks
            .filter { deck -> deck.id in deckIds }
            .flatMap { deck ->
                deck.cards
                    .filter { card -> isCardReadyForExercise(card, deck, now) }
                    .map { card -> cardToExerciseCard(card, deck) }
            }
            .shuffled()
        eventFlow.send(
            if (exerciseCards.isEmpty()) {
                NoCardIsReadyForExercise
            } else {
                ExerciseIsReady(Exercise.State(exerciseCards))
            }
        )
    }

    private fun isCardReadyForExercise(
        card: Card,
        deck: Deck,
        now: DateTime
    ): Boolean {
        return when {
            card.isLearned -> false
            card.lastAnsweredAt == null -> true
            deck.exercisePreference.intervalScheme == null -> true
            else -> {
                val intervals: List<Interval> =
                    deck.exercisePreference.intervalScheme!!.intervals
                val interval: Interval = intervals.find {
                    it.targetLevelOfKnowledge == card.levelOfKnowledge
                } ?: intervals.maxBy { it.targetLevelOfKnowledge }!!
                card.lastAnsweredAt!! + interval.value < now
            }
        }
    }

    private fun cardToExerciseCard(
        card: Card,
        deck: Deck
    ): ExerciseCard {
        val isReverse = when (deck.exercisePreference.cardReverse) {
            CardReverse.Off -> false
            CardReverse.On -> true
            CardReverse.EveryOtherLap -> (card.lap % 2) == 1
        }
        val baseExerciseCard = ExerciseCard.Base(
            id = SUID.id(),
            card = card,
            deck = deck,
            isReverse = isReverse,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            initialLevelOfKnowledge = card.levelOfKnowledge
        )
        return when (deck.exercisePreference.testMethod) {
            Off -> OffTestExerciseCard(baseExerciseCard)
            Manual -> ManualTestExerciseCard(baseExerciseCard)
            Quiz -> {
                val variants: List<Card?> = QuizComposer.compose(card, deck, isReverse)
                QuizTestExerciseCard(baseExerciseCard, variants)
            }
            Entry -> EntryTestExerciseCard(baseExerciseCard)
        }
    }
}