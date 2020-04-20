package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.entity.SpeakEvent
import com.odnovolov.forgetmenot.domain.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.soywiz.klock.*
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

// todo: use two EnumColumnAdapter() instead this
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

val speakEventsAdapter = object : ColumnAdapter<List<SpeakEvent>, String> {
    override fun encode(value: List<SpeakEvent>): String {
        return value.joinToString { speakEvent: SpeakEvent ->
            when (speakEvent) {
                SpeakQuestion -> "SPEAK_QUESTION"
                SpeakAnswer -> "SPEAK_ANSWER"
                is Delay -> speakEvent.timeSpan.seconds.toInt().toString()
            }
        }
    }

    override fun decode(databaseValue: String): List<SpeakEvent> {
        return databaseValue.split(", ")
            .map { chunk: String ->
                when (chunk) {
                    "SPEAK_QUESTION" -> SpeakQuestion
                    "SPEAK_ANSWER" -> SpeakAnswer
                    else -> {
                        val timeSpan: TimeSpan = chunk.toInt().seconds
                        Delay(timeSpan)
                    }
                }
            }
    }
}