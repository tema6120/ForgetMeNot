package com.odnovolov.forgetmenot.common.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.Database
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver

lateinit var database: Database
private lateinit var sqliteDriver: SqlDriver
const val DATABASE_NAME = "forgetmenot.db"

fun initDatabase(applicationContext: Context, isRestoring: Boolean) {
    //applicationContext.deleteDatabase(DATABASE_NAME)
    if (!::sqliteDriver.isInitialized) {
        initSqlDriver(applicationContext)
    }
    if (!isRestoring) {
        cleanUpDatabase()
        initDatabase()
        initFirstScreenState()
    } else if (!::database.isInitialized) {
        initDatabase()
    }
}

private fun initSqlDriver(applicationContext: Context) {
    sqliteDriver = AndroidSqliteDriver(
        schema = Database.Schema,
        context = applicationContext,
        name = DATABASE_NAME,
        callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
            override fun onConfigure(db: SupportSQLiteDatabase) {
                super.onConfigure(db)
                db.execSQL("PRAGMA foreign_keys = true")
                db.execSQL("PRAGMA recursive_triggers = true")
                if (BuildConfig.DEBUG) {
                    DbUtils.supportDb = db
                }
            }
        }
    )
}

private fun initDatabase() {
    database = Database(
        sqliteDriver,
        DeckReviewPreferences.Adapter(
            deckSortingAdapter = EnumColumnAdapter()
        ),
        ExercisePreference.Adapter(
            testMethodAdapter = testMethodAdapter
        ),
        Pronunciation.Adapter(
            questionLanguageAdapter = localeAdapter,
            answerLanguageAdapter = localeAdapter
        )
    )
}

// 'Temp' is marker for tables, views and triggers which survives android process death but does
// not user complete activity dismissal. Lifetime of these tables corresponds lifetime of android
// saved instance state. It helps avoid migrations
private fun cleanUpDatabase() {
    "BEGIN TRANSACTION".execSQL()
    "PRAGMA foreign_keys = false".execSQL()
    val cursor: SqlCursor =
        "SELECT type, name FROM sqlite_master WHERE name LIKE 'Temp%'".execQuerySQL()
    cursor.use {
        while (cursor.next()) {
            val type: String? = cursor.getString(0)
            val name: String? = cursor.getString(1)
            when (type) {
                "table" -> "DROP TABLE IF EXISTS $name".execSQL()
                "view" -> "DROP VIEW IF EXISTS $name".execSQL()
                "trigger" -> "DROP TRIGGER IF EXISTS $name".execSQL()
                "index" -> "DROP INDEX IF EXISTS $name".execSQL()
            }
        }
    }
    "PRAGMA foreign_keys = true".execSQL()
    "COMMIT TRANSACTION".execSQL()
    "VACUUM".execSQL()
}

fun initFirstScreenState() {
    with(database) {
        transaction {
            homeInitQueries.createTableHomeState()
            homeInitQueries.initHomeState()
            addDeckInitQueries.createTableAddDeckState()
            addDeckInitQueries.initAddDeckState()
        }
    }
}

private fun String.execQuerySQL(): SqlCursor = sqliteDriver.executeQuery(null, this, 0)

private fun String.execSQL(): Unit = sqliteDriver.execute(null, this, 0)