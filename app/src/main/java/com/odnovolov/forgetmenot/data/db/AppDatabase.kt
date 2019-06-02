package com.odnovolov.forgetmenot.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.data.db.entity.CardDbRow
import com.odnovolov.forgetmenot.data.db.entity.DeckDbRow
import com.odnovolov.forgetmenot.data.db.entity.ExerciseCardDbRow

@Database(
    entities = [DeckDbRow::class, CardDbRow::class, ExerciseCardDbRow::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val NAME = "ForgetMeNot.db"
    }

    abstract fun deckDao(): DeckDao
    abstract fun exerciseDao(): ExerciseDao
}