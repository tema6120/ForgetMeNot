package com.odnovolov.forgetmenot.persistence

import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent
import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting
import com.soywiz.klock.*
import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
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

val pronunciationEventsAdapter = object : ColumnAdapter<List<PronunciationEvent>, String> {
    override fun encode(value: List<PronunciationEvent>): String {
        return value.joinToString { pronunciationEvent: PronunciationEvent ->
            when (pronunciationEvent) {
                SpeakQuestion -> "SPEAK_QUESTION"
                SpeakAnswer -> "SPEAK_ANSWER"
                is Delay -> pronunciationEvent.timeSpan.seconds.toInt().toString()
            }
        }
    }

    override fun decode(databaseValue: String): List<PronunciationEvent> {
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

@Serializable
data class SerializableStringArray(
    val stringArray: Array<String?>
)

val stringArrayAdapter = object : ColumnAdapter<Array<String?>, String> {
    override fun encode(value: Array<String?>): String {
        val serializable = SerializableStringArray(value)
        return Json.encodeToString(SerializableStringArray.serializer(), serializable)
    }

    override fun decode(databaseValue: String): Array<String?> {
        val serializable = try {
            Json.decodeFromString(SerializableStringArray.serializer(), databaseValue)
        } catch (e: SerializationException) {
            return arrayOf()
        }
        return serializable.stringArray
    }
}