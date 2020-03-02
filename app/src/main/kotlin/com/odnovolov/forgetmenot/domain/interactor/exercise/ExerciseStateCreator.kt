package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.domain.generateId
import com.soywiz.klock.DateTime

class ExerciseStateCreator(
    private val globalState: GlobalState
) {
    class NoCardIsReadyForExercise(override val message: String) : Exception(message)

    fun create(deckIds: List<Long>, isWalkingMode: Boolean): Exercise.State {
        val now = DateTime.now()
        val notSortedExerciseCards: List<List<ExerciseCard>> = globalState.decks
            .filter { deck -> deck.id in deckIds }
            .map { deck ->
                var cards: List<Card> = deck.cards
                    .filter { card -> isCardReadyForExercise(card, deck, now) }
                val isRandom = deck.exercisePreference.randomOrder
                cards = sortCardsInDeck(cards, isRandom)
                cards.map { card -> cardToExerciseCard(card, deck, isWalkingMode) }
            }
        if (notSortedExerciseCards.flatten().isEmpty())
            throw NoCardIsReadyForExercise("No card is ready for exercise")
        val sortedExerciseCards: List<ExerciseCard> = sortExerciseCards(notSortedExerciseCards)
        return Exercise.State(sortedExerciseCards, isWalkingMode = isWalkingMode)
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

    private fun sortCardsInDeck(cards: List<Card>, isRandom: Boolean): List<Card> {
        return if (isRandom) {
            cards.shuffled().sortedBy { it.lap }
        } else {
            cards.sortedBy { it.lap }
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

    private fun sortExerciseCards(exerciseCards: List<List<ExerciseCard>>): List<ExerciseCard> {
        val totalSize = exerciseCards.sumBy { it.size }
        val sortedExerciseCardArray: Array<ExerciseCard?> = arrayOfNulls(totalSize)
        fun getVacantIndices(count: Int): List<Int> = sortedExerciseCardArray
            .mapIndexedNotNull { index, e -> if (e == null) index else null }
            .shuffled()
            .take(count)
            .sorted()
        exerciseCards.forEach { exerciseCardsInDeck: List<ExerciseCard> ->
            val vacantIndices: List<Int> = getVacantIndices(exerciseCardsInDeck.size)
            vacantIndices.forEachIndexed { index: Int, vacantIndex: Int ->
                val exerciseCard = exerciseCardsInDeck[index]
                sortedExerciseCardArray[vacantIndex] = exerciseCard
            }
        }
        return ArrayList<ExerciseCard>(sortedExerciseCardArray.size).apply {
            sortedExerciseCardArray.forEach { exerciseCard -> this.add(exerciseCard!!) }
        }
    }
}