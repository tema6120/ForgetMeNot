package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.CardInversion
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.TestingMethod.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise.Companion.CARD_FILTER_NO_LIMIT
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise
import kotlin.random.Random

class ExerciseStateCreatorWithFiltering(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(
        val decks: List<Deck>,
        val cardFilter: CardFilterForExercise
    )

    fun getCurrentMatchingCardsNumber(): Int {
        val allMatchingCardsNumber: Int = state.decks.sumBy { deck: Deck ->
            deck.cards.count { card: Card ->
                isCardAvailableForExercise(
                    card,
                    deck.exercisePreference.intervalScheme,
                    state.cardFilter
                )
            }
        }
        return  if (state.cardFilter.limit != CARD_FILTER_NO_LIMIT) {
            minOf(allMatchingCardsNumber, state.cardFilter.limit)
        } else {
            allMatchingCardsNumber
        }
    }

    fun hasAnyCardAvailableForExercise(): Boolean {
        return state.decks.any { deck: Deck ->
            deck.cards.any { card: Card ->
                isCardAvailableForExercise(
                    card,
                    deck.exercisePreference.intervalScheme,
                    state.cardFilter
                )
            }
        }
    }

    fun create(): Exercise.State {
        var exerciseCards: List<ExerciseCard> = state.decks
            .map { deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card ->
                        isCardAvailableForExercise(
                            card,
                            deck.exercisePreference.intervalScheme,
                            state.cardFilter
                        )
                    }
                    .let { cards: List<Card> -> if (isRandom) cards.shuffled() else cards }
                    .sortedBy { card: Card -> card.lap }
                    .map { card -> cardToExerciseCard(card, deck) }
            }
            .flattenWithShallowShuffling()
        val limit = state.cardFilter.limit
        if (limit > 0) {
            exerciseCards = exerciseCards.take(limit)
        }
        QuizComposer.clearCache()
        return Exercise.State(exerciseCards)
    }

    private fun cardToExerciseCard(
        card: Card,
        deck: Deck
    ): ExerciseCard {
        val isInverted = when (deck.exercisePreference.cardInversion) {
            CardInversion.Off -> false
            CardInversion.On -> true
            CardInversion.EveryOtherLap -> (card.lap % 2) == 1
            CardInversion.Randomly -> Random.nextBoolean()
        }
        val isWalkingMode = globalState.isWalkingModeEnabled
        val baseExerciseCard = ExerciseCard.Base(
            id = generateId(),
            card = card,
            deck = deck,
            isInverted = isInverted,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            timeLeft = if (isWalkingMode) 0 else deck.exercisePreference.timeForAnswer,
            initialGrade = card.grade,
            isGradeEditedManually = false
        )
        return when (deck.exercisePreference.testingMethod) {
            Off -> OffTestExerciseCard(baseExerciseCard)
            Manual -> ManualTestExerciseCard(baseExerciseCard)
            Quiz -> {
                if (isWalkingMode) {
                    ManualTestExerciseCard(baseExerciseCard)
                } else {
                    val variants: List<Card?> =
                        QuizComposer.compose(card, deck, isInverted, withCaching = true)
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