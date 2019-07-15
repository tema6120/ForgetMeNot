package com.odnovolov.forgetmenot.data.db

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
}