package com.odnovolov.forgetmenot.common.database

import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.common.entity.SpeakEvent
import com.odnovolov.forgetmenot.common.entity.SpeakEvent.*
import com.odnovolov.forgetmenot.common.entity.TestMethod
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.screen.home.adddeck.Stage
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

val stageAdapter = object : ColumnAdapter<Stage, String> {
    override fun encode(value: Stage): String {
        return value.name
    }

    override fun decode(databaseValue: String): Stage {
        return try {
            Stage.valueOf(databaseValue)
        } catch (e: IllegalArgumentException) {
            Stage.Idle
        }
    }
}

val listOfLocalesAdapter = object : ColumnAdapter<List<Locale>, String?> {
    override fun encode(value: List<Locale>): String? {
        return if (value.isEmpty()) null
        else value.joinToString(
            separator = ",",
            transform = localeAdapter::encode
        )
    }

    override fun decode(databaseValue: String?): List<Locale> {
        return databaseValue?.split(",")?.map(localeAdapter::decode) ?: emptyList()
    }
}

val nameCheckResultAdapter = object : ColumnAdapter<NameCheckResult, String> {
    override fun encode(value: NameCheckResult): String = value.name

    override fun decode(databaseValue: String): NameCheckResult =
        NameCheckResult.valueOf(databaseValue)
}

val namePresetDialogStatusAdapter =
    object : ColumnAdapter<NamePresetDialogStatus, String> {
        override fun encode(value: NamePresetDialogStatus): String = value.name

        override fun decode(databaseValue: String): NamePresetDialogStatus =
            NamePresetDialogStatus.valueOf(databaseValue)
    }

val testMethodAdapter = object : ColumnAdapter<TestMethod, String> {
    override fun encode(value: TestMethod): String {
        return value.name
    }

    override fun decode(databaseValue: String): TestMethod {
        return TestMethod.valueOf(databaseValue)
    }
}

val speakEventAdapter = object : ColumnAdapter<SpeakEvent, String> {
    override fun encode(value: SpeakEvent): String {
        return when (value) {
            SpeakQuestion -> "SPEAK_QUESTION"
            SpeakAnswer -> "SPEAK_ANSWER"
            is Delay -> "DELAY(${value.seconds})"
        }
    }

    override fun decode(databaseValue: String): SpeakEvent {
        return when (databaseValue) {
            "SPEAK_QUESTION" -> SpeakQuestion
            "SPEAK_ANSWER" -> SpeakAnswer
            else -> {
                val startIndex = databaseValue.indexOf('(') + 1
                val endIndex = databaseValue.length - 1
                val ms = databaseValue.subSequence(startIndex, endIndex).toString().toInt()
                Delay(ms)
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