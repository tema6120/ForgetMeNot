package com.odnovolov.forgetmenot.domain.interactor.exercise.example

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.CardInversion
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.TestingMethod
import com.odnovolov.forgetmenot.domain.entity.TestingMethod.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose.ToDemonstrateGradingSettings
import com.odnovolov.forgetmenot.domain.interactor.exercise.example.ExerciseExamplePurpose.ToDemonstratePronunciationSettings
import kotlin.random.Random

class ExampleExerciseStateCreator(
    private val deck: Deck
) {
    fun create(purpose: ExerciseExamplePurpose): Exercise.State {
        val doNotInvert: Boolean = purpose == ToDemonstratePronunciationSettings
        val isRandom = deck.exercisePreference.randomOrder
        val hasUnlearnedCard = deck.cards.any { card: Card -> !card.isLearned }
        val filteredCards =
            if (hasUnlearnedCard) {
                deck.cards.filter { card: Card -> !card.isLearned }
            } else {
                deck.cards
            }
        val exerciseCards: List<ExerciseCard> = when {
            filteredCards.isEmpty() -> emptyList()
            purpose == ToDemonstrateGradingSettings -> {
                val randomCard: Card = filteredCards.random()
                val copiedCard = Card(
                    id = -1,
                    question = randomCard.question,
                    answer = randomCard.answer,
                    grade = 4
                )
                val exerciseCard: ExerciseCard =
                    copiedCard.toExerciseCard(doNotInvert, testingMethod = Manual)
                listOf(exerciseCard)
            }
            else -> {
                filteredCards.let { cards: List<Card> ->
                    if (isRandom) cards.shuffled() else cards
                }
                    .map { card ->
                        card.toExerciseCard(doNotInvert, deck.exercisePreference.testingMethod)
                    }.also {
                        QuizComposer.clearCache()
                    }
            }
        }
        return Exercise.State(exerciseCards)
    }

    private fun Card.toExerciseCard(
        doNotInvert: Boolean,
        testingMethod: TestingMethod
    ): ExerciseCard {
        val isInverted =
            if (doNotInvert) {
                false
            } else {
                when (deck.exercisePreference.cardInversion) {
                    CardInversion.Off -> false
                    CardInversion.On -> true
                    CardInversion.EveryOtherLap -> (lap % 2) == 1
                    CardInversion.Randomly -> Random.nextBoolean()
                }
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
        return when (testingMethod) {
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