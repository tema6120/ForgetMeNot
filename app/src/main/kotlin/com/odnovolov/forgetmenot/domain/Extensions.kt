package com.odnovolov.forgetmenot.domain

import com.odnovolov.forgetmenot.domain.architecturecomponents.SUID
import com.odnovolov.forgetmenot.domain.entity.*
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.MonthSpan
import com.soywiz.klock.TimeSpan

fun MonthSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(this, TimeSpan(.0))

fun TimeSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(MonthSpan(0), this)

fun generateId(): Long = SUID.id()

fun ExercisePreference.isDefault(): Boolean = this.id == ExercisePreference.Default.id

fun ExercisePreference.isIndividual(): Boolean =
    this.id != ExercisePreference.Default.id && this.name.isEmpty()

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