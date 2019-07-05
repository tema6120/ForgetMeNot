package com.odnovolov.forgetmenot.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.odnovolov.forgetmenot.data.db.dao.Backuper
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.data.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.data.db.entity.DeckDbEntity
import com.odnovolov.forgetmenot.data.db.entity.ExerciseCardDbEntity

@Database(
    entities = [DeckDbEntity::class, CardDbEntity::class, ExerciseCardDbEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val NAME = "ForgetMeNot.db"
    }

    abstract fun deckDao(): DeckDao
    abstract fun exerciseDao(): ExerciseDao

    fun getBackuper(): Backuper {
        return Backuper(supportDb = openHelper.writableDatabase)
    }
}