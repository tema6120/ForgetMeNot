package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class ExercisePreference(
    override val id: Long,
    name: String,
    randomOrder: Boolean,
    pronunciation: Pronunciation,
    cardInversion: CardInversion,
    isQuestionDisplayed: Boolean,
    testingMethod: TestingMethod,
    intervalScheme: IntervalScheme?,
    grading: Grading,
    timeForAnswer: Int,
    pronunciationPlan: PronunciationPlan
) : FlowMakerWithRegistry<ExercisePreference>() {
    var name: String by flowMaker(name)
    var randomOrder: Boolean by flowMaker(randomOrder)
    var pronunciation: Pronunciation by flowMakerForCopyable(pronunciation)
    var cardInversion: CardInversion by flowMaker(cardInversion)
    var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
    var testingMethod: TestingMethod by flowMaker(testingMethod)
    var intervalScheme: IntervalScheme? by flowMakerForCopyable(intervalScheme)
    var grading: Grading by flowMakerForCopyable(grading)
    var timeForAnswer: Int by flowMaker(timeForAnswer)
    var pronunciationPlan: PronunciationPlan by flowMakerForCopyable(pronunciationPlan)

    override fun copy() = ExercisePreference(
        id,
        name,
        randomOrder,
        pronunciation.copy(),
        cardInversion,
        isQuestionDisplayed,
        testingMethod,
        intervalScheme?.copy(),
        grading.copy(),
        timeForAnswer,
        pronunciationPlan.copy()
    )

    companion object {
        val Default by lazy {
            ExercisePreference(
                id = 0L,
                name = "",
                randomOrder = true,
                pronunciation = Pronunciation.Default,
                cardInversion = CardInversion.Off,
                isQuestionDisplayed = true,
                testingMethod = TestingMethod.Manual,
                intervalScheme = IntervalScheme.Default,
                grading = Grading.Default,
                timeForAnswer = DO_NOT_USE_TIMER,
                pronunciationPlan = PronunciationPlan.Default
            )
        }
    }
}

const val DO_NOT_USE_TIMER = 0

fun ExercisePreference.isDefault(): Boolean = this.id == ExercisePreference.Default.id

fun ExercisePreference.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun ExercisePreference.isShared(): Boolean = name.isNotEmpty()

fun checkExercisePreferenceName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedExercisePreferences.any { it.name == testingName } ->
            NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}