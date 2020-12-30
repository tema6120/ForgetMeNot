package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.entity.isDefault
import com.odnovolov.forgetmenot.domain.generateId
import com.soywiz.klock.DateTimeSpan

class IntervalsSettings(
    private val deckSettings: DeckSettings
) {
    private val intervalScheme: IntervalScheme?
        get() = deckSettings.state.deck.exercisePreference.intervalScheme

    private var lastIntervalScheme: IntervalScheme? = null

    fun turnOnIntervals() {
        if (intervalScheme != null) return
        val newIntervalScheme = lastIntervalScheme ?: IntervalScheme.Default
        deckSettings.setIntervalScheme(newIntervalScheme)
    }

    fun turnOffIntervals() {
        if (intervalScheme == null) return
        lastIntervalScheme = intervalScheme
        deckSettings.setIntervalScheme(null)
    }

    fun modifyInterval(grade: Int, newValue: DateTimeSpan) {
        val isValueChanged: Boolean =
            intervalScheme?.let { intervalScheme: IntervalScheme ->
                val oldValue = intervalScheme.intervals
                    .find { it.grade == grade }
                    ?.value ?: false
                oldValue != newValue
            } ?: false
        if (!isValueChanged) return
        updateIntervalScheme(
            createNewIndividualIntervalScheme = {
                val newIntervals: CopyableList<Interval> = IntervalScheme.Default.intervals
                    .map { defaultInterval: Interval ->
                        val value: DateTimeSpan =
                            if (defaultInterval.grade == grade)
                                newValue
                            else
                                defaultInterval.value
                        Interval(
                            id = generateId(),
                            grade = defaultInterval.grade,
                            value = value
                        )
                    }
                    .toCopyableList()
                IntervalScheme(id = generateId(), intervals = newIntervals)
            },
            updateCurrentIntervalScheme = {
                intervalScheme?.let { intervalScheme: IntervalScheme ->
                    intervalScheme.intervals
                        .find { it.grade == grade }
                        ?.value = newValue
                }
            }
        )
    }

    fun addInterval(value: DateTimeSpan) {
        if (intervalScheme == null) return
        fun newInterval() = Interval(
            id = generateId(),
            grade = intervalScheme!!.intervals.last().grade + 1,
            value = value
        )
        updateIntervalScheme(
            createNewIndividualIntervalScheme = {
                val newIntervals: CopyableList<Interval> =
                    (createIntervalsFromDefaultIntervals() + newInterval())
                        .toCopyableList()
                IntervalScheme(id = generateId(), intervals = newIntervals)
            },
            updateCurrentIntervalScheme = {
                intervalScheme?.let { intervalScheme: IntervalScheme ->
                    intervalScheme.intervals = (intervalScheme.intervals + newInterval())
                        .toCopyableList()
                }
            }
        )
    }

    fun removeLastInterval() {
        val hasAtLeastTwoIntervals: Boolean = intervalScheme
            ?.let { it.intervals.size >= 2 } ?: false
        if (!hasAtLeastTwoIntervals) return
        updateIntervalScheme(
            createNewIndividualIntervalScheme = {
                val newIntervals: CopyableList<Interval> = createIntervalsFromDefaultIntervals()
                    .dropLast(1)
                    .toCopyableList()
                IntervalScheme(id = generateId(), intervals = newIntervals)
            },
            updateCurrentIntervalScheme = {
                intervalScheme?.let { intervalScheme: IntervalScheme ->
                    intervalScheme.intervals = intervalScheme.intervals.dropLast(1).toCopyableList()
                }
            }
        )
    }

    private fun createIntervalsFromDefaultIntervals(): CopyableList<Interval> {
        return IntervalScheme.Default.intervals
            .map { defaultInterval: Interval ->
                Interval(
                    id = generateId(),
                    grade = defaultInterval.grade,
                    value = defaultInterval.value
                )
            }
            .toCopyableList()
    }

    private inline fun updateIntervalScheme(
        crossinline createNewIndividualIntervalScheme: () -> IntervalScheme,
        crossinline updateCurrentIntervalScheme: () -> Unit
    ) {
        intervalScheme?.let { oldIntervalScheme: IntervalScheme ->
            when {
                oldIntervalScheme.isDefault() -> {
                    val newIndividualIntervalScheme = createNewIndividualIntervalScheme()
                    deckSettings.setIntervalScheme(newIndividualIntervalScheme)
                }
                else -> {
                    updateCurrentIntervalScheme()
                    if (oldIntervalScheme.shouldBeDefault()) {
                        deckSettings.setIntervalScheme(IntervalScheme.Default)
                    }
                }
            }
        }
    }

    private fun IntervalScheme.shouldBeDefault(): Boolean {
        if (this.intervals.size != IntervalScheme.Default.intervals.size) return false
        repeat(this.intervals.size) { index ->
            val thisInterval = this.intervals[index]
            val defaultInterval = IntervalScheme.Default.intervals[index]
            if (thisInterval.grade != defaultInterval.grade
                || thisInterval.value != defaultInterval.value
            ) {
                return false
            }
        }
        return true
    }
}