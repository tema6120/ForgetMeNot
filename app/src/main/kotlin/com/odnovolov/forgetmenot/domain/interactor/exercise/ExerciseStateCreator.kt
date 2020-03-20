package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.soywiz.klock.DateTime

class ExerciseStateCreator(
    private val globalState: GlobalState
) {
    class NoCardIsReadyForExercise(override val message: String) : Exception(message)

    fun create(deckIds: List<Long>, isWalkingMode: Boolean): Exercise.State {
        val now = DateTime.now()
        val exerciseCards: List<ExerciseCard> = globalState.decks
            .filter { deck -> deck.id in deckIds }
            .map { deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card -> isCardReadyForExercise(card, deck, now) }
                    .let { cards: List<Card> -> if (isRandom) cards.shuffled() else cards }
                    .sortedBy { card: Card -> card.lap }
                    .map { card -> cardToExerciseCard(card, deck, isWalkingMode) }
            }
            .also { notSortedExerciseCards: List<List<ExerciseCard>> ->
                if (notSortedExerciseCards.flatten().isEmpty())
                    throw NoCardIsReadyForExercise("No card is ready for exercise")
            }
            .flattenWithShallowShuffling()
        return Exercise.State(exerciseCards, isWalkingMode = isWalkingMode)
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
        deck: Deck,
        isWalkingMode: Boolean
    ): ExerciseCard {
        val isReverse = when (deck.exercisePreference.cardReverse) {
            CardReverse.Off -> false
            CardReverse.On -> true
            CardReverse.EveryOtherLap -> (card.lap % 2) == 1
        }
        val baseExerciseCard = ExerciseCard.Base(
            id = generateId(),
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
                if (isWalkingMode) {
                    ManualTestExerciseCard(baseExerciseCard)
                } else {
                    val variants: List<Card?> = QuizComposer.compose(card, deck, isReverse)
                    QuizTestExerciseCard(baseExerciseCard, variants)
                }
            }
            Entry -> {
                if (isWalkingMode) {
                    ManualTestExerciseCard(baseExerciseCard)
                } else {
                    EntryTestExerciseCard(baseExerciseCard)
                }
            }
        }
    }
}