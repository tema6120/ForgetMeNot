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
private const val BACKUP_PREFIX = "BACKUP_FORGET_ME_NOT_"

fun initDatabase(applicationContext: Context, isRestoring: Boolean) {
    //applicationContext.deleteDatabase(DATABASE_NAME)
    if (!::sqliteDriver.isInitialized) {
        initSqlDriver(applicationContext)
        initDatabaseInstance()
        createTemporaryStructures()
        transaction {
            if (isRestoring) {
                restoreTemporaryTablesDataFromBackup()
            }
            deleteBackupOfTemporaryTables()
        }
        if (!isRestoring) {
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
                db.execSQL("PRAGMA foreign_keys = true")
                db.execSQL("PRAGMA recursive_triggers = true")
                db.execSQL("PRAGMA temp_store = MEMORY")
                if (BuildConfig.DEBUG) {
                    DbUtils.supportDb = db
                }
            }
        }
    )
}

private fun initDatabaseInstance() {
    database = Database(
        sqliteDriver,
        DeckReviewPreferences.Adapter(
            deckSortingAdapter = EnumColumnAdapter()
        ),
        ExercisePreference.Adapter(
            testMethodAdapter = testMethodAdapter,
            cardReverseAdapter = EnumColumnAdapter()
        ),
        Pronunciation.Adapter(
            questionLanguageAdapter = localeAdapter,
            answerLanguageAdapter = localeAdapter
        )
    )
}

fun createTemporaryStructures() {
    with(database) {
        database.transaction {
            with (temporaryTablesQueries) {
                homeState()
                deckSelection()
                addDeckState()
                exercise()
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
                transtionFromIndividualToDefaultBeforeUpdateOnExercisePreference()
                transitionToDefaultAfterDeleteOnExercisePreference()
                deleteUnusedIndividualExercisePreference()
                clenupAfterDeleteOfExercisePreference()
                preventRemovalOfDefaultIntervalScheme()
                transitionToDefaultAfterDeleteOnIntervalScheme()
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
                transitionToDefaultAfterDeleteOnPronunciation()
                deleteUnusedIndividualPronunciation()
            }
        }
    }
}

private fun transaction(block: () -> Unit) {
    "BEGIN TRANSACTION".execSQL()
    block()
    "COMMIT TRANSACTION".execSQL()
}

fun restoreTemporaryTablesDataFromBackup() {
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

fun deleteBackupOfTemporaryTables() {
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

fun initFirstScreenState() {
    with(database.firstScreenInitQueries) {
        initHomeState()
        initAddDeckState()
    }
}

fun backUpTemporaryTables() {
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
    "VACUUM".execSQL()
}

private fun String.execQuerySQL(): SqlCursor = sqliteDriver.executeQuery(null, this, 0)

private fun String.execSQL(): Unit = sqliteDriver.execute(null, this, 0)