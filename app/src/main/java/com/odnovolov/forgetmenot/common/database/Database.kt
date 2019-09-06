package com.odnovolov.forgetmenot.common.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odnovolov.forgetmenot.Database
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver

lateinit var database: Database
const val DATABASE_NAME = "forgetmenot.db"

fun initDatabase(applicationContext: Context) {
    //applicationContext.deleteDatabase(DATABASE_NAME)
    val driver = AndroidSqliteDriver(
        schema = Database.Schema,
        context = applicationContext,
        name = DATABASE_NAME,
        callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
            override fun onConfigure(db: SupportSQLiteDatabase) {
                super.onConfigure(db)
                db.execSQL("PRAGMA foreign_keys = true")
                db.execSQL("PRAGMA recursive_triggers = true")
                DbUtils.supportDb = db
            }
        }
    )
    database = Database(
        driver,
        Deck.Adapter(
            createdAtAdapter = calendarAdapter,
            lastOpenedAtAdapter = calendarAdapter
        ),
        DeckSorting.Adapter(
            deckSortingAdapter = EnumColumnAdapter()
        ),
        Pronunciation.Adapter(
            questionLanguageAdapter = localeAdapter,
            answerLanguageAdapter = localeAdapter
        )
    )
    with(database) {
        transaction {
            homeInitQueries.createTableHomeState()
            homeInitQueries.initHomeState()
            addDeckInitQueries.createTableAddDeckState()
            addDeckInitQueries.initAddDeckState()
        }
    }
}