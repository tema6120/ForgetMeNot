package com.odnovolov.forgetmenot.domain.interactor.exercise.example

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.CardInversion
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.TestingMethod.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.*

class ExampleExerciseStateCreator(
    private val deck: Deck
) {
    fun create(): Exercise.State {
        val isRandom = deck.exercisePreference.randomOrder
        val exerciseCards: List<ExerciseCard> = deck.cards
            .let { cards: List<Card> ->
                val hasUnlearnedCard = cards.any { card: Card -> !card.isLearned }
                if (hasUnlearnedCard)
                    cards.filter { card: Card -> !card.isLearned }
                else
                    cards
            }
            .let { cards: List<Card> -> if (isRandom) cards.shuffled() else cards }
            .map { card -> card.toExerciseCard() }
        QuizComposer.clearCache()
        return Exercise.State(exerciseCards)
    }

    private fun Card.toExerciseCard(): ExerciseCard {
        val isInverted = when (deck.exercisePreference.cardInversion) {
            CardInversion.Off -> false
            CardInversion.On -> true
            CardInversion.EveryOtherLap -> (lap % 2) == 1
        }
        val baseExerciseCard = ExerciseCard.Base(
            id = generateId(),
            card = this,
            deck = deck,
            isInverted = isInverted,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            timeLeft = deck.exercisePreference.timeForAnswer,
            initialGrade = grade,
            isGradeEditedManually = false
        )
        return when (deck.exercisePreference.testingMethod) {
            Off -> OffTestExerciseCard(baseExerciseCard)
            Manual -> ManualTestExerciseCard(baseExerciseCard)
            Quiz -> {
                val variants: List<Card?> =
                    QuizComposer.compose(this, deck, isInverted, withCaching = true)
                QuizTestExerciseCard(baseExerciseCard, variants)
            }
            Entry -> {
                EntryTestExerciseCard(baseExerciseCard)
            }
        }
    }
}