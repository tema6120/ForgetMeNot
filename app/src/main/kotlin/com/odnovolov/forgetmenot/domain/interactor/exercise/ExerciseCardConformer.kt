package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.CardInversion.*
import com.odnovolov.forgetmenot.domain.generateId
import kotlin.random.Random

class ExerciseCardConformer(
    private val state: Exercise.State,
    private val globalState: GlobalState
) {
    private val isWalkingMode
        get() = globalState.isWalkingModeEnabled

    private lateinit var newExerciseCards: MutableList<ExerciseCard>

    fun conform() {
        newExerciseCards = state.exerciseCards.toMutableList()
        deleteCardsAccordingToNewGradingSettings()
        conformExerciseCards()
        recalculateGrades()
        addRetestedCardsAccordingToNewGradingSettings()
        state.exerciseCards = newExerciseCards
        QuizComposer.clearCache()
    }

    private fun deleteCardsAccordingToNewGradingSettings() {
        newExerciseCards = newExerciseCards.filterIndexed { position, exerciseCard ->
            val isRetestingPermitted = exerciseCard.base.deck.exercisePreference.grading.askAgain
            if (isRetestingPermitted) return@filterIndexed true
            val currentCardId = exerciseCard.base.card.id
            for (positionBefore: Int in 0 until position) {
                val cardIdBefore = state.exerciseCards[positionBefore].base.card.id
                if (cardIdBefore == currentCardId) {
                    return@filterIndexed false
                }
            }
            return@filterIndexed true
        }
            .toMutableList()
    }

    private fun conformExerciseCards() {
        newExerciseCards = newExerciseCards.map { exerciseCard: ExerciseCard ->
            if (exerciseCard.isAnswered) {
                return@map exerciseCard
            }
            val requiredTestingMethod = determineTestingMethod(exerciseCard)
            if (requiredTestingMethod != exerciseCard.testingMethod) {
                recreateExerciseCard(exerciseCard, requiredTestingMethod)
            } else {
                exerciseCard.apply {
                    conformToCardInversionSettings()
                    conformToQuestionDisplaySettings()
                    conformToTimerSettings()
                }
            }
        }
            .toMutableList()
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
        val deck: Deck = exerciseCard.base.deck
        val card = exerciseCard.base.card
        val isInverted: Boolean = when (exerciseCard.base.deck.exercisePreference.cardInversion) {
            Off -> false
            On -> true
            EveryOtherLap -> {
                val wasAnswered: Boolean =
                    state.exerciseCards.any { it.base.card.id == card.id && it.isAnswered }
                when {
                    wasAnswered -> card.lap % 2 == 0
                    else -> card.lap % 2 == 1
                }
            }
            Randomly -> Random.nextBoolean()
        }
        val timeLeft: Int = when {
            isWalkingMode || exerciseCard.base.card.isLearned -> 0
            else -> exerciseCard.base.deck.exercisePreference.timeForAnswer
        }
        val baseExerciseCard = ExerciseCard.Base(
            id = exerciseCard.base.id,
            card = card,
            deck = deck,
            isInverted = isInverted,
            isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
            timeLeft = timeLeft,
            initialGrade = card.grade,
            isGradeEditedManually = exerciseCard.base.isGradeEditedManually
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

    private fun ExerciseCard.conformToCardInversionSettings() {
        val card = base.card
        val needToInvert: Boolean =
            when (base.deck.exercisePreference.cardInversion) {
                Off -> false
                On -> true
                EveryOtherLap -> {
                    val wasAnswered: Boolean =
                        state.exerciseCards.any { it.base.card.id == card.id && it.isAnswered }
                    when {
                        wasAnswered -> card.lap % 2 == 0
                        else -> card.lap % 2 == 1
                    }
                }
                Randomly -> Random.nextBoolean()
            }
        if (base.isInverted != needToInvert) {
            base.isInverted = needToInvert
            base.hint = null
        }
    }

    private fun ExerciseCard.conformToQuestionDisplaySettings() {
        base.isQuestionDisplayed = base.deck.exercisePreference.isQuestionDisplayed
    }

    private fun ExerciseCard.conformToTimerSettings() {
        base.timeLeft = when {
            isWalkingMode || this.base.card.isLearned -> 0
            else -> this.base.deck.exercisePreference.timeForAnswer
        }
    }

    private fun recalculateGrades() {
        newExerciseCards
            .filter { exerciseCard: ExerciseCard -> !exerciseCard.base.isGradeEditedManually }
            .groupBy { exerciseCard: ExerciseCard -> exerciseCard.base.card.id }
            .forEach { (_, exerciseCards: List<ExerciseCard>) ->
                if (exerciseCards.isEmpty()) return@forEach
                val base: ExerciseCard.Base = exerciseCards.first().base
                val grading: Grading = base.deck.exercisePreference.grading
                var calculatingGrade = base.initialGrade
                var isFirstAnswer = true
                for (exerciseCard: ExerciseCard in exerciseCards) {
                    calculatingGrade =
                        applyGradeChange(exerciseCard, grading, calculatingGrade, isFirstAnswer)
                    isFirstAnswer = false
                }
                val card: Card = base.card
                if (card.grade != calculatingGrade) {
                    card.grade = calculatingGrade
                }
            }
    }

    private fun applyGradeChange(
        exerciseCard: ExerciseCard,
        grading: Grading,
        gradeBeforeAnswer: Int,
        isFirstAnswer: Boolean
    ): Int {
        val gradeChange: GradeChange = when (exerciseCard.base.isAnswerCorrect) {
            null -> return gradeBeforeAnswer
            true -> {
                if (isFirstAnswer)
                    grading.onFirstCorrectAnswer else
                    grading.onRepeatedCorrectAnswer
            }
            false -> {
                if (isFirstAnswer)
                    grading.onFirstWrongAnswer else
                    grading.onRepeatedWrongAnswer
            }
        }
        return gradeChange.apply(gradeBeforeAnswer)
    }

    private fun addRetestedCardsAccordingToNewGradingSettings() {
        val exerciseCards = newExerciseCards.toList()
        exerciseCards.forEachIndexed { position, exerciseCard ->
            val isRetestingPermitted = exerciseCard.base.deck.exercisePreference.grading.askAgain
            if (!isRetestingPermitted) return@forEachIndexed
            val shouldBeRetestedCard = exerciseCard.base.isAnswerCorrect == false
            if (!shouldBeRetestedCard) return@forEachIndexed
            val currentCardId = exerciseCard.base.card.id
            var hasRetestedCard = false
            for (positionAfter: Int in position + 1..exerciseCards.lastIndex) {
                val cardIdAfter = exerciseCards[positionAfter].base.card.id
                if (cardIdAfter == currentCardId) {
                    hasRetestedCard = true
                    break
                }
            }
            if (!hasRetestedCard) {
                addRetestingCardFrom(exerciseCard)
            }
        }
    }

    private fun addRetestingCardFrom(exerciseCard: ExerciseCard) {
        val baseExerciseCard = with(exerciseCard.base) {
            ExerciseCard.Base(
                id = generateId(),
                card = card,
                deck = deck,
                isInverted = isInverted,
                isQuestionDisplayed = deck.exercisePreference.isQuestionDisplayed,
                timeLeft = if (isWalkingMode) DO_NOT_USE_TIMER else deck.exercisePreference.timeForAnswer,
                initialGrade = initialGrade,
                isGradeEditedManually = isGradeEditedManually
            )
        }
        val retestingExerciseCard: ExerciseCard =
            when (exerciseCard.base.deck.exercisePreference.testingMethod) {
                TestingMethod.Off -> OffTestExerciseCard(baseExerciseCard)
                TestingMethod.Manual -> ManualTestExerciseCard(baseExerciseCard)
                TestingMethod.Quiz -> {
                    if (isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        val variants: List<Card?> = with(baseExerciseCard) {
                            QuizComposer.compose(card, deck, isInverted, withCaching = false)
                        }
                        QuizTestExerciseCard(baseExerciseCard, variants)
                    }
                }
                TestingMethod.Entry -> {
                    if (isWalkingMode) {
                        ManualTestExerciseCard(baseExerciseCard)
                    } else {
                        EntryTestExerciseCard(baseExerciseCard)
                    }
                }
            }
        newExerciseCards.add(retestingExerciseCard)
    }
}