package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class ExercisePreference(
    override val id: Long,
    name: String,
    randomOrder: Boolean,
    testingMethod: TestingMethod,
    intervalScheme: IntervalScheme?,
    pronunciation: Pronunciation,
    isQuestionDisplayed: Boolean,
    cardInversion: CardInversion,
    pronunciationPlan: PronunciationPlan,
    timeForAnswer: Int
) : FlowMakerWithRegistry<ExercisePreference>() {
    var name: String by flowMaker(name)
    var randomOrder: Boolean by flowMaker(randomOrder)
    var testingMethod: TestingMethod by flowMaker(testingMethod)
    var intervalScheme: IntervalScheme? by flowMaker(intervalScheme)
    var pronunciation: Pronunciation by flowMaker(pronunciation)
    var isQuestionDisplayed: Boolean by flowMaker(isQuestionDisplayed)
    var cardInversion: CardInversion by flowMaker(cardInversion)
    var pronunciationPlan: PronunciationPlan by flowMaker(pronunciationPlan)
    var timeForAnswer: Int by flowMaker(timeForAnswer)

    override fun copy() = ExercisePreference(
        id,
        name,
        randomOrder,
        testingMethod,
        intervalScheme?.copy(),
        pronunciation.copy(),
        isQuestionDisplayed,
        cardInversion,
        pronunciationPlan.copy(),
        timeForAnswer
    )

    companion object {
        val Default by lazy {
            ExercisePreference(
                id = 0L,
                name = "",
                randomOrder = true,
                testingMethod = TestingMethod.Manual,
                intervalScheme = IntervalScheme.Default,
                pronunciation = Pronunciation.Default,
                isQuestionDisplayed = true,
                cardInversion = CardInversion.Off,
                pronunciationPlan = PronunciationPlan.Default,
                timeForAnswer = NOT_TO_USE_TIMER
            )
        }
    }
}

const val NOT_TO_USE_TIMER = 0

fun ExercisePreference.isDefault(): Boolean = this.id == ExercisePreference.Default.id

fun ExercisePreference.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkExercisePreferenceName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedExercisePreferences.any { it.name == testingName } ->
            NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}