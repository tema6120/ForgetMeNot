package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.*

class ExerciseCardConformer(
    private val state: Exercise.State,
    private val globalState: GlobalState
) {
    private val isWalkingMode
        get() = globalState.isWalkingModeEnabled

    fun conform() {
        var isExerciseCardsListChanged = false
        val newExerciseCards: List<ExerciseCard> =
            state.exerciseCards.map { exerciseCard: ExerciseCard ->
                if (exerciseCard.isAnswered) {
                    return@map exerciseCard
                }
                val requiredTestingMethod = determineTestingMethod(exerciseCard)
                if (requiredTestingMethod != exerciseCard.testingMethod) {
                    isExerciseCardsListChanged = true
                    recreateExerciseCard(exerciseCard, requiredTestingMethod)
                } else {
                    exerciseCard.apply { conformToExercisePreference() }
                }
            }
        if (isExerciseCardsListChanged) {
            state.exerciseCards = newExerciseCards
        }
        QuizComposer.clearCache()
    }

    private fun determineTestingMethod(exerciseCard: ExerciseCard): TestingMethod {
        return when (exerciseCard.base.deck.exercisePreference.testingMethod) {
            TestingMethod.Off -> TestingMethod.Off
            TestingMethod.Manual -> TestingMethod.Manual
            TestingMethod.Quiz -> if (isWalkingMode) TestingMethod.Manual else TestingMethod.Quiz
            TestingMethod.Entry -> if (isWalkingMode) TestingMethod.Manual else TestingMethod.Entry
        }
    }

    private val ExerciseCard.testingMethod: TestingMethod
        get() = when (this) {
            is OffTestExerciseCard -> TestingMethod.Off
            is ManualTestExerciseCard -> TestingMethod.Manual
            is QuizTestExerciseCard -> TestingMethod.Quiz
            is EntryTestExerciseCard -> TestingMethod.Entry
            else -> error("unknown testing method")
        }

    private fun recreateExerciseCard(
        exerciseCard: ExerciseCard,
        testingMethod: TestingMethod
    ): ExerciseCard {
        val card: Card = exerciseCard.base.card
        val deck: Deck = exerciseCard.base.deck
        val isInverted: Boolean = needToInvert(exerciseCard)
        val timeLeft: Int = determineTimeLeft(exerciseCard)
        val baseExerciseCard = ExerciseCard.Base(
            id = exerciseCard.base.id,
            card = card,
            deck = deck,
            isInverted = isInverted,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            timeLeft = timeLeft,
            initialGrade = card.grade,
            isGradeEditedManually = false
        )
        return when (testingMethod) {
            TestingMethod.Off -> OffTestExerciseCard(baseExerciseCard)
            TestingMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
            TestingMethod.Quiz -> {
                val variants: List<Card?> =
                    QuizComposer.compose(card, deck, isInverted, withCaching = true)
                QuizTestExerciseCard(baseExerciseCard, variants)
            }
            TestingMethod.Entry -> {
                EntryTestExerciseCard(baseExerciseCard)
            }
        }
    }

    private fun ExerciseCard.conformToExercisePreference() {
        if (base.isInverted != needToInvert(this)) {
            base.isInverted = needToInvert(this)
            base.hint = null
        }
        base.isQuestionDisplayed = base.deck.exercisePreference.isQuestionDisplayed
        base.timeLeft = determineTimeLeft(this)
    }

    private fun needToInvert(exerciseCard: ExerciseCard): Boolean {
        val card = exerciseCard.base.card
        return when (exerciseCard.base.deck.exercisePreference.cardInversion) {
            CardInversion.Off -> false
            CardInversion.On -> true
            CardInversion.EveryOtherLap -> {
                val wasAnswered: Boolean =
                    state.exerciseCards.any { it.base.card.id == card.id && it.isAnswered }
                when {
                    wasAnswered -> card.lap % 2 == 0
                    else -> card.lap % 2 == 1
                }
            }
        }
    }

    private fun determineTimeLeft(exerciseCard: ExerciseCard): Int {
        return when {
            isWalkingMode || exerciseCard.base.card.isLearned -> 0
            else -> exerciseCard.base.deck.exercisePreference.timeForAnswer
        }
    }
}