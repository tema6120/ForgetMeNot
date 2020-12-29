package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.TestingMethod.*
import com.odnovolov.forgetmenot.domain.flattenWithShallowShuffling
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isCardAvailableForExercise

class ExerciseStateCreator(
    private val globalState: GlobalState
) {
    fun hasAnyCardAvailableForExercise(deckIds: List<Long>): Boolean {
        return globalState.decks.any { deck: Deck ->
            deck.id in deckIds && deck.cards.any { card: Card ->
                isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme)
            }
        }
    }

    fun create(deckIds: List<Long>): Exercise.State {
        val exerciseCards: List<ExerciseCard> = globalState.decks
            .filter { deck -> deck.id in deckIds }
            .map { deck ->
                val isRandom = deck.exercisePreference.randomOrder
                deck.cards
                    .filter { card ->
                        isCardAvailableForExercise(card, deck.exercisePreference.intervalScheme)
                    }
                    .let { cards: List<Card> -> if (isRandom) cards.shuffled() else cards }
                    .sortedBy { card: Card -> card.lap }
                    .map { card -> cardToExerciseCard(card, deck) }
            }
            .flattenWithShallowShuffling()
        QuizComposer.clearCache()
        if (exerciseCards.isEmpty()) throw NoCardIsReadyForExercise
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

    object NoCardIsReadyForExercise : Exception("No card is ready for exercise")
}