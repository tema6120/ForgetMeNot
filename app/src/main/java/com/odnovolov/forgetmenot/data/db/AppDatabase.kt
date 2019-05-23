package com.odnovolov.forgetmenot.data.db

import androidx.room.Database
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

    companion object {
        const val NAME = "ForgetMeNot.db"
    }

    abstract fun deckDao(): DeckDao
}