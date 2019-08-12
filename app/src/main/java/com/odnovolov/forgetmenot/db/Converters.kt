package com.odnovolov.forgetmenot.db

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun timestampToCalendar(timeStamp: Long?): Calendar? {
        return if (timeStamp == null) {
            null
        } else {
            Calendar.getInstance()
                .apply { timeInMillis = timeStamp }
        }
    }

    @TypeConverter
    fun localeToString(locale: Locale?): String? {
        return locale?.toLanguageTag()
    }

    @TypeConverter
    fun stringToLocale(languageTag: String?): Locale? {
        return if (languageTag == null) {
            null
        } else {
            Locale.forLanguageTag(languageTag)
        }
    }

}