package com.odnovolov.forgetmenot.domain

import com.odnovolov.forgetmenot.domain.architecturecomponents.SUID
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.CardFilterLastTested
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.MonthSpan
import com.soywiz.klock.TimeSpan

fun MonthSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(this, TimeSpan(.0))

fun TimeSpan.toDateTimeSpan(): DateTimeSpan = DateTimeSpan(MonthSpan(0), this)

fun generateId(): Long = SUID.id()

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

fun isCardAvailableForExercise(
    card: Card,
    intervalScheme: IntervalScheme?,
    cardFilter: CardFilterForExercise? = null
): Boolean {
    return when {
        card.isLearned -> false
        cardFilter != null && card.grade !in cardFilter.gradeRange -> false
        cardFilter != null && !doesCardMatchLastTestedFilter(card, cardFilter) -> false
        intervalScheme == null -> true
        card.lastTestedAt == null -> true
        else -> {
            val intervals: List<Interval> = intervalScheme.intervals
            val interval: Interval = intervals.find { it.grade == card.grade }
                ?: intervals.maxByOrNull { it.grade }!!
            card.lastTestedAt!! + interval.value < DateTime.now()
        }
    }
}

fun doesCardMatchLastTestedFilter(
    card: Card,
    cardFilterLastTested: CardFilterLastTested
): Boolean {
    val now: DateTime = DateTime.now()
    val lastTestedAt: DateTime? = card.lastTestedAt
    val lastTestedFromTimeAgo = cardFilterLastTested.lastTestedFromTimeAgo
    val lastTestedToTimeAgo = cardFilterLastTested.lastTestedToTimeAgo
    return if (lastTestedAt == null) {
        lastTestedFromTimeAgo == null
    } else {
        (lastTestedFromTimeAgo == null || lastTestedAt > now - lastTestedFromTimeAgo)
                &&
                (lastTestedToTimeAgo == null || lastTestedAt < now - lastTestedToTimeAgo)
    }
}

fun <E> MutableList<E>.removeFirst(predicate: (E) -> Boolean): E? {
    for (i in indices) {
        val element = get(i)
        if (predicate(element)) {
            removeAt(i)
            return element
        }
    }
    return null
}