package com.odnovolov.forgetmenot.common.database

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import androidx.sqlite.db.SupportSQLiteDatabase
import com.odnovolov.forgetmenot.BuildConfig
import com.odnovolov.forgetmenot.Database
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlCursor
import com.squareup.sqldelight.db.SqlDriver

lateinit var database: Database

object DatabaseLifecycleManager : ActivityLifecycleCallbacks {
    private lateinit var sqliteDriver: SqlDriver
    private const val DATABASE_NAME = "forgetmenot.db"
    private const val BACKUP_PREFIX = "BACKUP_FORGET_ME_NOT_"

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        //activity.deleteDatabase(DATABASE_NAME)
        val isActivityFirstCreated = savedInstanceState == null
        if (!::sqliteDriver.isInitialized) {
            initSqlDriver(activity.applicationContext)
            initDatabase()
            createTemporaryStructures()
            transaction {
                if (!isActivityFirstCreated) {
                    restoreTemporaryTablesDataFromBackup()
                }
                deleteBackupOfTemporaryTables()
            }
            "VACUUM".execSQL()
            if (isActivityFirstCreated) {
                initFirstScreenState()
            }
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
                    db.execSQL("PRAGMA foreign_keys = ON")
                    db.execSQL("PRAGMA recursive_triggers = ON")
                    db.execSQL("PRAGMA temp_store = MEMORY")
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
            DeckReviewSorting.Adapter(
                criterionAdapter = EnumColumnAdapter(),
                directionAdapter = EnumColumnAdapter()
            ),
            Pronunciation.Adapter(
                questionLanguageAdapter = localeAdapter,
                answerLanguageAdapter = localeAdapter
            ),
            ExercisePreference.Adapter(
                testMethodAdapter = EnumColumnAdapter(),
                cardReverseAdapter = EnumColumnAdapter()
            ),
            WalkingModePreference.Adapter(
                keyGestureAdapter = EnumColumnAdapter(),
                keyGestureActionAdapter = EnumColumnAdapter()
            )
        )
    }

    private fun createTemporaryStructures() {
        with(database) {
            transaction {
                with(temporaryTablesQueries) {
                    homeState()
                    deckSelection()
                    addDeckState()
                    exercise()
                    textSelection()
                    events()
                    exerciseCard()
                    quiz()
                    answerInput()
                    editCardState()
                    deckSettingsState()
                    intervalsState()
                    modifyIntervalState()
                    pronunciationState()
                }
                with(temporaryViewsQueries) {
                    currentExerciseCard()
                    currentExercisePronunciation()
                    currentExercisePreference()
                }
                with(temporaryTriggersQueries) {
                    observeAnswerAutoSpeakEvent()
                    preventRemovalOfDefaultExercisePreference()
                    transitionFromDefaultToIndividualBeforeUpdateOnExercisePreference()
                    transtionFromIndividualToDefaultAfterUpdateOnExercisePreference()
                    deleteUnusedIndividualExercisePreference()
                    clenupAfterDeleteOfExercisePreference()
                    preventRemovalOfDefaultIntervalScheme()
                    transitionFromDefaultToIndividualBeforeDeleteOnInterval()
                    transitionFromIndividualToDefaultAfterDeleteOnInterval()
                    transitionFromDefaultToSharedBeforeUpdateOnIntervalScheme()
                    transitionFromDefaultToIndividualBeforeUpdateOnInterval()
                    transitionFromIndividualToDefaultAfterUpdateOnInterval()
                    transitionFromDefaultToIndividualBeforeInsertOnInterval()
                    transitionFromIndividualToDefaultWhenInsertOnInterval()
                    deleteUnusedIndividualIntervalScheme()
                    preventRemovalOfDefaultPronunciation()
                    transitionFromDefaultBeforeUpdateOnPronunciation()
                    transitionToDefaultBeforeUpdateOnPronunciation()
                    deleteUnusedIndividualPronunciation()
                }
            }
        }
    }

    private fun restoreTemporaryTablesDataFromBackup() {
        val cursor: SqlCursor =
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE '$BACKUP_PREFIX%'"
                .execQuerySQL()
        cursor.use {
            while (cursor.next()) {
                val backupTableName: String? = cursor.getString(0)
                val temporaryTableName = backupTableName!!.substring(BACKUP_PREFIX.length)
                "INSERT INTO $temporaryTableName SELECT * FROM $backupTableName".execSQL()
            }
        }
    }

    private fun deleteBackupOfTemporaryTables() {
        val cursor: SqlCursor =
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name LIKE '$BACKUP_PREFIX%'"
                .execQuerySQL()
        cursor.use {
            while (cursor.next()) {
                val tableName: String? = cursor.getString(0)
                "DROP TABLE IF EXISTS $tableName".execSQL()
            }
        }
    }

    private fun initFirstScreenState() {
        with(database.firstScreenInitQueries) {
            initHomeState()
            initAddDeckState()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        transaction {
            deleteBackupOfTemporaryTables()
            val cursor: SqlCursor =
                "SELECT name FROM sqlite_temp_master WHERE type = 'table'".execQuerySQL()
            cursor.use {
                while (cursor.next()) {
                    val tableName: String? = cursor.getString(0)
                    "CREATE TABLE $BACKUP_PREFIX$tableName AS SELECT * FROM $tableName".execSQL()
                }
            }
        }
    }

    private fun transaction(block: () -> Unit) {
        "BEGIN TRANSACTION".execSQL()
        block()
        "COMMIT TRANSACTION".execSQL()
    }

    private fun String.execQuerySQL(): SqlCursor = sqliteDriver.executeQuery(null, this, 0)

    private fun String.execSQL(): Unit = sqliteDriver.execute(null, this, 0)

    // Unused callbacks
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
}