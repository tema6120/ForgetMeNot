package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.soywiz.klock.DateTime
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.MonthSpan
import com.soywiz.klock.TimeSpan
import com.squareup.sqldelight.ColumnAdapter
import java.util.*

val localeAdapter = object : ColumnAdapter<Locale, String> {
    override fun encode(value: Locale): String {
        return value.toLanguageTag()
    }

    override fun decode(databaseValue: String): Locale {
        return Locale.forLanguageTag(databaseValue)
    }
}

val speakEventsAdapter = object : ColumnAdapter<List<SpeakEvent>, String> {
    // example of databaseValue: "SPEAK_QUESTION, DELAY(2), SPEAK_ANSWER, DELAY(1)"

    override fun encode(value: List<SpeakEvent>): String {
        return value.map {
            when (it) {
                SpeakQuestion -> "SPEAK_QUESTION"
                SpeakAnswer -> "SPEAK_ANSWER"
                is Delay -> "DELAY(${it.timeSpan.millisecondsLong})"
            }
        }
            .joinToString()
    }

    override fun decode(databaseValue: String): List<SpeakEvent> {
        return databaseValue.split(", ")
            .map {
                when (it) {
                    "SPEAK_QUESTION" -> SpeakQuestion
                    "SPEAK_ANSWER" -> SpeakAnswer
                    else -> {
                        val startIndex = databaseValue.indexOf('(') + 1
                        val endIndex = databaseValue.length - 1
                        val ms = it.subSequence(startIndex, endIndex).toString().toDouble()
                        Delay(TimeSpan(ms))
                    }
                }
            }
    }
}

val dateTimeAdapter = object : ColumnAdapter<DateTime, Long> {
    override fun encode(value: DateTime): Long = value.unixMillisLong
    override fun decode(databaseValue: Long): DateTime = DateTime.fromUnix(databaseValue)
}

val dateTimeSpanAdapter = object : ColumnAdapter<DateTimeSpan, String> {
    override fun encode(value: DateTimeSpan): String {
        return "${value.monthSpan.totalMonths}|${value.timeSpan.millisecondsLong}"
    }

    override fun decode(databaseValue: String): DateTimeSpan {
        val chunks = databaseValue.split("|")
        val totalMonths: Int = chunks[0].toInt()
        val monthSpan = MonthSpan(totalMonths)
        val milliseconds: Double = chunks[1].toDouble()
        val timeSpan = TimeSpan(milliseconds)
        return DateTimeSpan(monthSpan, timeSpan)
    }
}

val deckSortingAdapter = object : ColumnAdapter<DeckSorting, String> {
    override fun encode(value: DeckSorting): String {
        return "${value.criterion} ${value.direction}"
    }

    override fun decode(databaseValue: String): DeckSorting {
        return databaseValue.split(" ").let {
            val criterion = DeckSorting.Criterion.valueOf(it[0])
            val direction = DeckSorting.Direction.valueOf(it[1])
            DeckSorting(criterion, direction)
        }
    }
}