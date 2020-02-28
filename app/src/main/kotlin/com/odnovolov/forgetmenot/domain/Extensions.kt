package com.odnovolov.forgetmenot.domain

import com.odnovolov.forgetmenot.domain.architecturecomponents.SUID
import com.odnovolov.forgetmenot.domain.entity.*
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.MonthSpan
import com.soywiz.klock.TimeSpan

fun MonthSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(this, TimeSpan(.0))

fun TimeSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(MonthSpan(0), this)

fun generateId(): Long = SUID.id()

fun ExercisePreference.shallowCopy(
    id: Long,
    name: String = this.name,
    randomOrder: Boolean = this.randomOrder,
    testMethod: TestMethod = this.testMethod,
    intervalScheme: IntervalScheme? = this.intervalScheme,
    pronunciation: Pronunciation = this.pronunciation,
    isQuestionDisplayed: Boolean = this.isQuestionDisplayed,
    cardReverse: CardReverse = this.cardReverse
) = ExercisePreference(
    id,
    name,
    randomOrder,
    testMethod,
    intervalScheme,
    pronunciation,
    isQuestionDisplayed,
    cardReverse
)

fun ExercisePreference.isDefault(): Boolean = this.id == ExercisePreference.Default.id

fun ExercisePreference.isIndividual(): Boolean =
    this.id != ExercisePreference.Default.id && this.name.isEmpty()

fun ExercisePreference.shouldBeDefault(): Boolean {
    return this.shallowCopy(id = ExercisePreference.Default.id) == ExercisePreference.Default
}

fun IntervalScheme.isDefault(): Boolean = this.id == IntervalScheme.Default.id

fun IntervalScheme.isIndividual(): Boolean =
    this.id != IntervalScheme.Default.id && this.name.isEmpty()

fun Pronunciation.isDefault(): Boolean = this.id == Pronunciation.Default.id

fun Pronunciation.isIndividual(): Boolean =
    this.id != Pronunciation.Default.id && this.name.isEmpty()

fun checkDeckName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.decks.any { it.name == testedName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

fun checkExercisePreferenceName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedExercisePreferences.any { it.name == testedName } ->
            NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

fun checkIntervalSchemeName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedIntervalSchemes.any { it.name == testedName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

fun checkPronunciationName(testedName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testedName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedPronunciations.any { it.name == testedName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}