package com.odnovolov.forgetmenot.domain

import com.odnovolov.forgetmenot.domain.architecturecomponents.SUID
import com.odnovolov.forgetmenot.domain.entity.*
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.MonthSpan
import com.soywiz.klock.TimeSpan

fun MonthSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(this, TimeSpan(.0))

fun TimeSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(MonthSpan(0), this)

fun generateId(): Long = SUID.id()

fun ExercisePreference.isDefault(): Boolean = this.id == ExercisePreference.Default.id

fun ExercisePreference.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun IntervalScheme.isDefault(): Boolean = this.id == IntervalScheme.Default.id

fun IntervalScheme.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun RepetitionSetting.isDefault(): Boolean = this.id == RepetitionSetting.Default.id

fun RepetitionSetting.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkDeckName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.decks.any { it.name == testingName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

fun checkExercisePreferenceName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedExercisePreferences.any { it.name == testingName } ->
            NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

fun checkIntervalSchemeName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedIntervalSchemes.any { it.name == testingName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

fun checkRepetitionSettingName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedRepetitionSettings.any { it.name == testingName } -> NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}

// shuffle items but preserve inner list order
fun <T> List<List<T>>.flattenWithShallowShuffling(): List<T> {
    val totalSize = this.sumBy { it.size }
    if (totalSize == 0) return emptyList()
    val result: MutableList<T?> = ArrayList(totalSize)
    val indices: MutableList<Int> = ArrayList(totalSize)
    repeat(totalSize) { index: Int ->
        result.add(null)
        indices.add(index)
    }
    fun extractIndices(count: Int): List<Int> {
        indices.shuffle()
        val vacantIndices = ArrayList<Int>(count)
        repeat(count) {
            vacantIndices.add(indices.removeAt(0))
        }
        vacantIndices.sort()
        return vacantIndices
    }
    this.forEach { innerList: List<T> ->
        val count = innerList.size
        val vacantIndices: List<Int> = extractIndices(count)
        repeat(count) { time ->
            val vacantIndex = vacantIndices[time]
            val item: T = innerList[time]
            result[vacantIndex] = item
        }
    }
    return ArrayList<T>(totalSize).apply {
        result.forEach { item -> this.add(item!!) }
    }
}

fun isCardAvailableForExercise(testingCard: Card, intervalScheme: IntervalScheme?): Boolean {
    return when {
        testingCard.isLearned -> false
        intervalScheme == null -> true
        testingCard.lastAnsweredAt == null -> true
        else -> {
            val intervals: List<Interval> = intervalScheme.intervals
            val interval: Interval = intervals.find {
                it.levelOfKnowledge == testingCard.levelOfKnowledge
            } ?: intervals.maxByOrNull { it.levelOfKnowledge }!!
            testingCard.lastAnsweredAt!! + interval.value < DateTime.now()
        }
    }
}