package com.odnovolov.forgetmenot.persistence

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.globalstate.*
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.logs.LogSqliteDriver

const val DATABASE_NAME = "forgetmenot.db"

object DatabaseInitializer {
    fun initSqlDriver(applicationContext: Context): SqlDriver {
        //applicationContext.deleteDatabase(DATABASE_NAME)
        val androidSqliteDriver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = applicationContext,
            name = DATABASE_NAME,
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    db.execSQL("PRAGMA foreign_keys = ON")
                    if (BuildConfig.DEBUG) {
                        DbUtils.supportDb = db
                    }
                }
            }
        )
        return if (BuildConfig.DEBUG) {
            LogSqliteDriver(androidSqliteDriver) { Log.d("db", it) }
        } else {
            androidSqliteDriver
        }
    }

    fun initDatabase(sqliteDriver: SqlDriver): Database {
        return Database(
            sqliteDriver,
            CardDb.Adapter(
                lastTestedAtAdapter = dateTimeAdapter
            ),
            DeckDb.Adapter(
                createdAtAdapter = dateTimeAdapter,
                lastTestedAtAdapter = dateTimeAdapter
            ),
            DeckListDb.Adapter(
                deckIdsAdapter = setOfLongAdapter
            ),
            DeckReviewPreferenceDb.Adapter(
                deckSortingCriterionAdapter = EnumColumnAdapter(),
                deckSortingDirectionAdapter = EnumColumnAdapter()
            ),
            ExercisePreferenceDb.Adapter(
                testingMethodAdapter = EnumColumnAdapter(),
                cardInversionAdapter = EnumColumnAdapter()
            ),
            FileFormatDb.Adapter(
                quoteModeAdapter = EnumColumnAdapter()
            ),
            IntervalDb.Adapter(
                valueAdapter = dateTimeSpanAdapter
            ),
            KeyGestureMapDb.Adapter(
                keyGestureAdapter = EnumColumnAdapter(),
                keyGestureActionAdapter = EnumColumnAdapter()
            ),
            PronunciationDb.Adapter(
                questionLanguageAdapter = localeAdapter,
                answerLanguageAdapter = localeAdapter
            ),
            PronunciationPlanDb.Adapter(
                pronunciationEventsAdapter = pronunciationEventsAdapter
            ),
            TipStateDb.Adapter(
                lastShowedAtAdapter = dateTimeAdapter
            )
        )
    }
}