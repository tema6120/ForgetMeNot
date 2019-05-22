package com.odnovolov.forgetmenot.data.db

import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.data.db.entity.DbCard
import com.odnovolov.forgetmenot.data.db.entity.DbDeck

@Database(
    entities = [DbDeck::class, DbCard::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao

    companion object {
        private const val DATABASE_NAME = "ForgetMeNot.db"
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            //context.deleteDatabase(DATABASE_NAME)
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .build()
        }
    }

    fun debugQuery(query: String): String {
        var cursor: Cursor? = null
        try {
            cursor = openHelper.readableDatabase.query(query)
            return DatabaseUtils.dumpCursorToString(cursor)
        } finally {
            cursor?.close()
        }
    }
}