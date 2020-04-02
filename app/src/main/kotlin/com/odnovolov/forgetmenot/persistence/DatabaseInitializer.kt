package com.odnovolov.forgetmenot.persistence

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.globalstate.ExercisePreferenceDb
import com.odnovolov.forgetmenot.persistence.globalstate.IntervalDb
import com.odnovolov.forgetmenot.persistence.globalstate.PronunciationDb
import com.odnovolov.forgetmenot.persistence.globalstate.RepetitionSettingDb
import com.odnovolov.forgetmenot.presentation.common.MainActivity
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.logs.LogSqliteDriver

object DatabaseInitializer : ActivityLifecycleCallbacks {
    private var isInitialized = false

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is MainActivity) return
        //activity.deleteDatabase(DATABASE_NAME)
        if (!isInitialized) {
            val sqliteDriver: SqlDriver = initSqlDriver(activity.applicationContext)
            initDatabase(sqliteDriver)
            val isActivityFirstCreated = savedInstanceState == null
            if (isActivityFirstCreated) {
                cleanupDatabase(sqliteDriver)
            }
            isInitialized = true
        }
    }

    private fun initSqlDriver(applicationContext: Context): SqlDriver {
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

    private fun initDatabase(sqliteDriver: SqlDriver) {
        database = Database(
            sqliteDriver,
            /*CardDb.Adapter(
                lastAnsweredAtAdapter = dateTimeAdapter
            ),
            DeckDb.Adapter(
                createdAtAdapter = dateTimeAdapter,
                lastOpenedAtAdapter = dateTimeAdapter
            ),*/
            DeckReviewPreferenceDb.Adapter(
                deckSortingAdapter = deckSortingAdapter
            ),
            ExercisePreferenceDb.Adapter(
                testMethodAdapter = EnumColumnAdapter(),
                cardReverseAdapter = EnumColumnAdapter()
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
            RepetitionSettingDb.Adapter(
                lastAnswerFromTimeAgoAdapter = dateTimeSpanAdapter,
                lastAnswerToTimeAgoAdapter = dateTimeSpanAdapter
            )
        )
    }

    private fun cleanupDatabase(sqliteDriver: SqlDriver) {
        database.serializableQueries.deleteAll()
        database.exercisePreferenceQueries.deleteUnused()
        database.intervalSchemeQueries.deleteUnused()
        database.pronunciationQueries.deleteUnused()
        database.speakPlanQueries.deleteUnused()
        database.repetitionSettingQueries.deleteUnused()
        sqliteDriver.executeQuery(null, "VACUUM", 0)
    }

    // Unused callbacks
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}