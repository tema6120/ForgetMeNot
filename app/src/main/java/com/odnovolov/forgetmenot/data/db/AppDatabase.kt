package com.odnovolov.forgetmenot.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.odnovolov.forgetmenot.data.db.dao.Backuper
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.data.db.entity.CardDbEntity
import com.odnovolov.forgetmenot.data.db.entity.DeckDbEntity
import com.odnovolov.forgetmenot.data.db.entity.ExerciseCardDbEntity
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDao
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckDao

@Database(
    entities = [DeckDbEntity::class, CardDbEntity::class, ExerciseCardDbEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun deckSettingsDao(): DeckSettingsDao
    abstract fun addDeckDao(): AddDeckDao

    fun getBackuper(): Backuper {
        return Backuper(supportDb = openHelper.writableDatabase)
    }

    companion object {
        const val NAME = "ForgetMeNot.db"

        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, NAME)
                .build()
        }
    }
}