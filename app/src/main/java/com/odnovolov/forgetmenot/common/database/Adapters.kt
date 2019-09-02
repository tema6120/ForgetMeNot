package com.odnovolov.forgetmenot.common.database

import com.odnovolov.forgetmenot.common.NameCheckResult
import com.odnovolov.forgetmenot.home.adddeck.Stage
import com.squareup.sqldelight.ColumnAdapter
import java.util.*

val calendarAdapter = object : ColumnAdapter<Calendar, Long> {
    override fun encode(value: Calendar): Long {
        return value.timeInMillis
    }

    override fun decode(databaseValue: Long): Calendar {
        return Calendar.getInstance()
            .apply { timeInMillis = databaseValue }
    }
}

val localeAdapter = object : ColumnAdapter<Locale, String> {
    override fun encode(value: Locale): String {
        return value.toLanguageTag()
    }

    override fun decode(databaseValue: String): Locale {
        return Locale.forLanguageTag(databaseValue)
    }
}

// Adapters that we use manually
// (SqlDelight doesn't currently take adapters if you create table with label)

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

val listOfLocalesAdapter = object : ColumnAdapter<List<Locale>, String> {
    override fun encode(value: List<Locale>): String {
        return value.joinToString(
            separator = ",",
            transform = localeAdapter::encode
        )
    }

    override fun decode(databaseValue: String): List<Locale> {
        return databaseValue.split(",")
            .map(localeAdapter::decode)
    }
}

val nameCheckStatusAdapter = object : ColumnAdapter<NameCheckResult, String> {
    override fun encode(value: NameCheckResult): String = value.name

    override fun decode(databaseValue: String): NameCheckResult =
        NameCheckResult.valueOf(databaseValue)
}

fun Long.asBoolean() = this == 1L