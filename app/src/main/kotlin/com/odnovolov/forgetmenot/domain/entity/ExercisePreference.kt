package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class ExercisePreference(
    override val id: Long,
    name: String,
    randomOrder: Boolean,
    testMethod: TestMethod,
    intervalScheme: IntervalScheme?,
    pronunciation: Pronunciation,
    isQuestionDisplayed: Boolean,
    cardReverse: CardReverse,
    pronunciationPlan: PronunciationPlan,
    timeForAnswer: Int
) : RegistrableFlowableState<ExercisePreference>() {
    var name: String by me(name)
    var randomOrder: Boolean by me(randomOrder)
    var testMethod: TestMethod by me(testMethod)
    var intervalScheme: IntervalScheme? by me(intervalScheme)
    var pronunciation: Pronunciation by me(pronunciation)
    var isQuestionDisplayed: Boolean by me(isQuestionDisplayed)
    var cardReverse: CardReverse by me(cardReverse)
    var pronunciationPlan: PronunciationPlan by me(pronunciationPlan)
    var timeForAnswer: Int by me(timeForAnswer)

    override fun copy() = ExercisePreference(
        id,
        name,
        randomOrder,
        testMethod,
        intervalScheme?.copy(),
        pronunciation.copy(),
        isQuestionDisplayed,
        cardReverse,
        pronunciationPlan.copy(),
        timeForAnswer
    )

    companion object {
        val Default by lazy {
            ExercisePreference(
                id = 0L,
                name = "",
                randomOrder = true,
                testMethod = TestMethod.Manual,
                intervalScheme = IntervalScheme.Default,
                pronunciation = Pronunciation.Default,
                isQuestionDisplayed = true,
                cardReverse = CardReverse.Off,
                pronunciationPlan = PronunciationPlan.Default,
                timeForAnswer = 0 // that means 'do not use timer'
            )
        }
    }
}

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